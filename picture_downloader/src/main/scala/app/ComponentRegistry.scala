package app
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import services._

import scala.concurrent.ExecutionContext
object ComponentRegistry extends ProxyServiceComponentImpl with StreamerComponentImpl with ConfigsComponentImpl with BaseSystem {

  override val appConfig: AppConfigsImpl = new AppConfigsImpl
  override val kafkaConfig: KafkaConfigs = new KafkaConfigsImpl
  override def streamer: ComponentRegistry.Streamer = new StreamerImpl()
  override def proxyService(implicit ec: ExecutionContext): ComponentRegistry.ProxyService = new RemoteProxyKeeper(BasicHttpCredentials("downloader", appConfig.proxyKeeperPassword))
}
