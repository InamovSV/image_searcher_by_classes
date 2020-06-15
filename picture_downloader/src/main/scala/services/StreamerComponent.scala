package services

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

trait StreamerComponent {

  def streamer: Streamer

  trait Streamer {
    def runKafkaStream(threads: Int)(implicit ec: ExecutionContext, as: ActorSystem, mat: Materializer): Consumer.Control

    def runDeleteStream(chunk: Int)(implicit as: ActorSystem, mat: Materializer): Consumer.Control
  }
}
