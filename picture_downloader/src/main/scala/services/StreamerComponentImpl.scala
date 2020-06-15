package services

import java.awt.Point
import java.io.File

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.{ActorAttributes, Materializer, Supervision}
import akka.stream.scaladsl.Sink
import app.BaseSystem
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser.decode
import io.circe.syntax._
import model._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import util.{RecursiveDecider, RecursiveDeciders}

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Try}

trait StreamerComponentImpl extends StreamerComponent {
  self: StreamerComponent with ConfigsComponent with BaseSystem with ProxyServiceComponent =>

  class StreamerImpl extends Streamer with LazyLogging {

    private val nnProducer: KafkaProducer[String, String] = new KafkaProducer[String, String](kafkaConfig.neuralProducerConfig)

    private val ds = new StringDeserializer

    private val consumerSettings =
      ConsumerSettings(actorSystem, ds, new ByteArrayDeserializer)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
        .withProperties(kafkaConfig.clickerConsumerConfig.asScala.toMap)

    private def handleClickerRequest(req: ClickerRequest, proxy: Option[SimpleProxy])(implicit ec: ExecutionContext, mat: Materializer): Future[NMRequest] = {

      val ownPath = s"${appConfig.picturesPath}/${req.id}"
      new File(ownPath).mkdir()
      val nnPath = s"${appConfig.nnPicturesPath}/${req.id}"
      (if (req.dimension == 1) {
        println(s"Start handling unglued pictures ${if (proxy.isEmpty) "without proxy"}")
        val ps = req.pictures.map { p =>
          (new Point(p.index / 10, p.index % 10), p.url)
        }

        util.downloadImages(ps, ownPath, nnPath, req.`class`, appConfig.downloadTimeout, proxy)
      } else {
        val firstPicture = req.pictures.head
        val rows = if (req.dimension == 2) 4 else req.dimension
        val cols = req.dimension
        println(s"Start handling glued pictures ${rows}x$cols ${if (proxy.isEmpty) "without proxy"}")
        util.downloadAndRiffle(firstPicture.url, rows.toInt, cols.toInt, ownPath, nnPath, req.`class`, appConfig.downloadTimeout, proxy)
      }).map(_ (req.id))
    }.andThen {
      case Failure(exception) =>
        println(exception.toString + "\n" + exception.getStackTraceString)
    }

    private val handleRequestWithProxyStrategy: PartialFunction[Throwable, RecursiveDecider] = {
      case e: EmptyProxyException => RecursiveDeciders.Retry(e, Option(1.5 second))
      case exception: Exception => RecursiveDeciders.Retry(exception)
    }

    override def runKafkaStream(threads: Int)(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer): Consumer.Control = {
      Consumer
        .committableSource(consumerSettings, Subscriptions.topics(kafkaConfig.clickerConsumerTopic))
        .mapAsyncUnordered(threads) { msg =>
          Future {
            val dMsg = ds.deserialize(kafkaConfig.clickerConsumerTopic, msg.record.value())
            val req = decode[ClickerRequest](dMsg).toOption
            println(req.map(t => s"Got request ${t.id}.").getOrElse(s"Got a task. Parse failed for massage: $dMsg"))

            req
          }
        }
        .mapAsyncUnordered(threads) {
          case Some(req) =>
            (if (appConfig.proxyEnabled) {
              util.futurecWithDecider[Option[NMRequest]](() => proxyService.getProxy().flatMap {
                case ProxyResponse(proxy@Some(_)) =>
                  handleClickerRequest(req, proxy).map(Option(_))
                case ProxyResponse(None) =>
                  throw new EmptyProxyException("Proxy service response contains no proxy")
              },
                x => x.isDefined,
                actorSystem.scheduler,
                Duration.Zero,
                3,
                exceptionStrategy = handleRequestWithProxyStrategy)
            } else {
              util.futurecWithDecider[Option[NMRequest]](
                () => handleClickerRequest(req, None).map(Option(_)),
                x => x.isDefined,
                actorSystem.scheduler,
                Duration.Zero,
                retries = 2
              )
            }).andThen {
              case Failure(exception: EndOfRetryException) =>
                println(s"Error during handling task ${req.id}")
                println(exception.getAccumulatedExceptionsWithStacktrace)
              case Failure(exception) =>
                println(s"Error during handling task ${req.id}: ${exception.toString}")
                println(exception.getStackTraceString)
            }

          case None =>
            Future.successful((Option.empty[NMRequest], 0))
        }
        .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
        .to(Sink.foreach {
          case (Some(req), _) =>
            nnProducer.send(new ProducerRecord(kafkaConfig.neuralProducerTopic, req.asJson.noSpaces))
          case (None, _) =>
            println("Empty request")
        })
        .run()
    }

    override def runDeleteStream(chunk: Int)(implicit as: ActorSystem, mat: Materializer): Consumer.Control =
      Consumer
        .committableSource(consumerSettings, Subscriptions.topics(kafkaConfig.neuralConsumerTopic))
        .map { msg =>
          val dMsg = ds.deserialize(kafkaConfig.neuralConsumerTopic, msg.record.value())
          val req = decode[NMResponse](dMsg).toOption
          println(req.map(t => s"Got request ${t.id}.").getOrElse(s"Got a task. Parse failed for massage: $dMsg"))

          req
        }
        .grouped(chunk)
        .to(Sink.foreach { responses =>
          responses.foreach(_.foreach { r =>
            Try(org.apache.commons.io.FileUtils.deleteDirectory(new File(s"${appConfig.picturesPath}/${r.id}")))
              .recover{
                case exception: Exception =>
                  println(s"Failed to delete folder. Cause: ${exception.toString}\n${exception.getStackTraceString}")
              }
              .foreach{_ =>
                println(s"Deleted folder ${appConfig.picturesPath}/${r.id}")
              }
          })
        })
        .run()
  }

}
