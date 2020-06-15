package services
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Sink
import app.BaseSystem
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.syntax._
import model.ProxyReturnRequest._
import model.{ProxyResponse, ProxyReturnRequest, ProxyStatus}

import scala.concurrent.{ExecutionContext, Future}

trait ProxyServiceComponentImpl extends ProxyServiceComponent with FailFastCirceSupport{
  self: BaseSystem with ConfigsComponent =>

  class RemoteProxyKeeper(cred: BasicHttpCredentials)(implicit ec: ExecutionContext) extends ProxyService{
    def getProxy() = {
      akka.http.scaladsl.Http().singleRequest(HttpRequest(uri = appConfig.proxyKeeperUrl + "/getProxy").addCredentials(cred))
        .flatMap { x =>
          x.entity.dataBytes
            .map(bytes => bytes.utf8String)
            .runWith(Sink.foreach(str => println("Got response from proxyKeeper: " + str))).flatMap(_ =>
            Unmarshal(x.entity).to[ProxyResponse]
          )
        }
    }

    def returnProxy(id: String, status: ProxyStatus): Future[HttpResponse] = {
      println(s"Returning proxy: $id to ${appConfig.proxyKeeperUrl}/returnProxy")
      Http().singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = appConfig.proxyKeeperUrl + "/returnProxy",
        entity = HttpEntity(ProxyReturnRequest(id, status).asJson.noSpaces)
          .withContentType(ContentTypes.`application/json`)
      ).addCredentials(cred))
    }
  }
}
