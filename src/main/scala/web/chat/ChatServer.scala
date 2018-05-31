package chat

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object ChatServer extends App {

  implicit val system: ActorSystem = ActorSystem("chatAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = system.settings.config
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")

  val service = new WebService

  val binding = Http().bindAndHandle(service.routes, interface, port)

  println(s"Server online at http://$interface:$port/")

  Await.result(system.whenTerminated, Duration.Inf)
}
