package app

import java.util.concurrent.Executors

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext

object Program extends BaseSystem with App {
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(ComponentRegistry.appConfig.threads))
  val stream = ComponentRegistry.streamer.runKafkaStream(ComponentRegistry.appConfig.inletThreads)

  val executionContextForDelStream = Option(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4)))
  val actorSystemForDelStream = ActorSystem("delSystem", defaultExecutionContext = executionContextForDelStream)

  val delStream = ComponentRegistry.streamer.runDeleteStream(ComponentRegistry.appConfig.deleteChunk)
  sys.addShutdownHook {
    stream.stop()
    stream.shutdown()
    delStream.stop()
    delStream.shutdown()
  }
}
