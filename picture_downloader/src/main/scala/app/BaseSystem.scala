package app

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait BaseSystem {
1
  implicit val actorSystem: ActorSystem = ActorSystem("downloader")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
