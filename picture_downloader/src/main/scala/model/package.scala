import io.circe.generic.JsonCodec
import repository.HasId

package object model {

  @JsonCodec
  case class Estimate(id: Long, step: Int, time: Int, threads: Option[Int], version: Int = 0) extends HasId

  @JsonCodec
  case class ClickerRequest(id: String, `class`: String, pictures: List[NNPicture], dimension: Byte)

  @JsonCodec
  case class NNPicture(index: Int, url: String)

  @JsonCodec
  case class NMRequest(id: String, path: String, `type`: String, indexes: Seq[Int])

  @JsonCodec
  case class SimpleProxy(id: String, host: String, port: Int, `type`: String) {
    val fullAddress = s"$host:$port"
  }

  object SimpleProxy {
    def apply(id: String, host: String, port: Int, `type`: String): SimpleProxy = new SimpleProxy(id, host, port, `type`)

    def apply(id: String, address: String, `type`: String): SimpleProxy = {
      val host = address.takeWhile(_ != ':')
      val port = address.dropWhile(_ != ':').tail.toInt
      new SimpleProxy(id, host, port, `type`)
    }
  }

  @JsonCodec
  case class ProxyResponse(proxy: Option[SimpleProxy])

  @JsonCodec
  case class ProxyReturnRequest(id: String, status: String)

  object ProxyReturnRequest {
    implicit def statusToString(status: ProxyStatus): String = status.toString
  }

  object GetProxyStatus

  sealed abstract class ProxyStatus(stringStatus: String) {
    override def toString: String = stringStatus
  }

  object ProxyStatus {

    case object GoodProxy extends ProxyStatus("good")

    case object BadProxy extends ProxyStatus("bad")

    case object GreatProxy extends ProxyStatus("great")

  }

  class EmptyProxyException(msg: String) extends Exception(msg)

  case class EndOfRetryException(exceptions: List[Throwable] = List.empty) extends Exception {
    override def getMessage: String = "End of retry"

    def getAccumulatedExceptions = exceptions.reverse.zipWithIndex.map(x => s"${x._2} - ${x._1.toString}").mkString("\n\t")

    def getAccumulatedExceptionsWithStacktrace = exceptions.reverse.zipWithIndex.map(x => s"${x._2} - ${x._1.toString}\n\t${x._1.getStackTraceString}").mkString("\n\t")
  }

  @JsonCodec
  case class NMResponse(id: String)
}
