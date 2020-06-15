package services

import akka.http.scaladsl.model.HttpResponse
import model.{ProxyResponse, ProxyStatus}

import scala.concurrent.{ExecutionContext, Future}

trait ProxyServiceComponent {

  def proxyService(implicit ec: ExecutionContext): ProxyService

  trait ProxyService {

    def getProxy(): Future[ProxyResponse]

    def returnProxy(id: String, status: ProxyStatus): Future[HttpResponse]

  }

}
