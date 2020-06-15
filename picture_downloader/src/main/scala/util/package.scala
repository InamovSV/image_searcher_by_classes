import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import java.net.{InetSocketAddress, URL}

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.{ClientTransport, Http}
import akka.stream.Materializer
import akka.stream.scaladsl.StreamConverters
import com.typesafe.scalalogging.LazyLogging
import javax.imageio.ImageIO
import model.{EndOfRetryException, NMRequest, SimpleProxy}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

package object util extends LazyLogging{

  def downloadImage(url: java.net.URL, timeout: FiniteDuration, proxy: Option[SimpleProxy])(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer): Future[BufferedImage] = {

    proxy match {
      case Some(p) =>
        val httpsProxyTransport = ClientTransport.httpsProxy(InetSocketAddress.createUnresolved(p.host, p.port))

        val settings = ConnectionPoolSettings(as).withTransport(httpsProxyTransport).withIdleTimeout(timeout)

        Http().singleRequest(HttpRequest(uri = url.toString), settings = settings)
          .map(x => ImageIO.read(x.entity.dataBytes.runWith(
            StreamConverters.asInputStream(timeout)
          )))
      case None =>
        Http().singleRequest(HttpRequest(uri = url.toString))
          .map(x => ImageIO.read(x.entity.dataBytes.runWith(StreamConverters.asInputStream(timeout))))
    }
  }

  def riffling(img: BufferedImage, nOfX: Int, nOfY: Int) = {
    val height: Double = img.getHeight
    val width: Double = img.getWidth
    val h: Int = (height / nOfY).toInt
    val w: Int = (width / nOfX).toInt
    for {
      y <- 0 until nOfY
      x <- 0 until nOfX
    } yield {
      (img.getSubimage(w * x, h * y, w, h), s"${y + 1}${x + 1}".toInt)
    }
  }


  def downloadAndRiffle(imgSrc: String, rows: Int, cols: Int, folderPath: String, nnPath: String, `class`: String, timeout: FiniteDuration, proxyAddress: Option[SimpleProxy])(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer) = {

    val url = new URL(imgSrc)
    val image = futurecWithDecider[BufferedImage](
      () => downloadImage(url, timeout, proxyAddress).andThen{
        case Failure(exception) =>
          println("downloadAndRiffle failed", exception)
      },
      x => true,
      as.scheduler,
      1 second,
      2
    ).map(_._1)
    image.map { bfImg =>
      new File(folderPath + s"/${`class`}").mkdir()

      val indexes = riffling(bfImg, cols, rows).map { singleBfImg =>

        val imgFile = new File(folderPath + s"/${`class`}/${singleBfImg._2}.jpeg")

        val isWritten = ImageIO.write(singleBfImg._1, "jpeg", imgFile)
        if (isWritten) {
          singleBfImg._2
        } else {
          throw new Exception("There's no appropriate writer in the system")
        }
      }

      NMRequest(_, nnPath, `class`, indexes)
    }.andThen{
      case Failure(exception) =>
        println("downloadAndRiffle failed", exception)
    }

  }

  def downloadImages(urls: Seq[(Point, String)], folderPath: String, nnPath: String, `class`: String, timeout: FiniteDuration, proxy: Option[SimpleProxy])(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer): Future[String => NMRequest] = {

    new File(folderPath + s"/${`class`}").mkdir()

    Future.sequence(urls.map {
      case (id, src) =>
        futurecWithDecider[Point](() => {
          downloadImage(new URL(src), timeout, proxy).map { image =>
            val imgFile = new File(folderPath + s"/${`class`}/${id.x}${id.y}.jpeg")
            val isWritten = ImageIO.write(image, "jpeg", imgFile)

            if (isWritten) {
              id
            } else {
              throw new Exception("There's no appropriate writer in the system")
            }
          }
        }, p => true, as.scheduler, 1 second, 2).map(_._1)

    }).map(x => NMRequest(_, nnPath, `class`, x.map(id => s"${id.x}${id.y}".toInt)))
  }


  trait RecursiveDecider{
    val exception: Throwable
  }

  object RecursiveDeciders {

    case class Retry(exception: Throwable, interval: Option[FiniteDuration] = None) extends RecursiveDecider

    case class Fail(exception: Throwable) extends RecursiveDecider

    case class Resume(exception: Throwable, interval: Option[FiniteDuration] = None) extends RecursiveDecider

  }

  val defaultExceptionStrategy: PartialFunction[Throwable, RecursiveDecider] = {
    case exception: Exception => RecursiveDeciders.Retry(exception)
  }

  def futurecWithDecider[T](callback: () => Future[T],
                                     condition: T => Boolean,
                                     scheduler: Scheduler,
                                     interval: FiniteDuration,
                                     retries: Int,
                                     init: FiniteDuration = Duration.Zero,
                                     exceptionStrategy: PartialFunction[Throwable, RecursiveDecider] = defaultExceptionStrategy)
                                    (implicit ec: ExecutionContext): Future[(T, Int)] = {

    def standardExceptionStrategy: PartialFunction[Throwable, Future[(T, Int)]] = {
      case exception: EndOfRetryException =>
        Future.failed(exception)
    }

    def decider(r: Int, exceptions: List[Throwable]): PartialFunction[Throwable, Future[(T, Int)]] = exceptionStrategy.andThen {
      case RecursiveDeciders.Resume(exception, intervalO) =>
        akka.pattern.after(interval, scheduler)(
          internalRecursive(callback, condition, scheduler, intervalO.getOrElse(interval), r, exceptions :+ exception)
        )

      case RecursiveDeciders.Retry(exception, intervalO) =>
        akka.pattern.after(interval, scheduler)(
          internalRecursive(callback, condition, scheduler, intervalO.getOrElse(interval), r - 1, exceptions :+ exception)
        )

      case RecursiveDeciders.Fail(exception) =>
        Future.failed(exception)
    }

    def internalRecursive(callback: () => Future[T],
                          condition: T => Boolean,
                          scheduler: Scheduler,
                          interval: FiniteDuration,
                          retry: Int,
                          exceptionAccum: List[Throwable] = List.empty): Future[(T, Int)] = {
      if (retry > 0) {
        callback() flatMap { res =>
          if (condition(res))
            Future.successful((res, retries - retry))
          else {
            akka.pattern.after(interval, scheduler)(
              internalRecursive(callback, condition, scheduler, interval, retry - 1, exceptionAccum)
            )
          }
        } recoverWith standardExceptionStrategy.orElse(decider(retry, exceptionAccum))
      } else {
        Future.failed(EndOfRetryException(exceptionAccum))
      }
    }

    akka.pattern.after(init, scheduler) {
      internalRecursive(callback, condition, scheduler, interval, retries)
    }

  }
}
