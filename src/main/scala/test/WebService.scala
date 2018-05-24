package test

import java.util.Date
import scala.util.Failure

import akka.actor._
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.{ Directives, Route }

import akka.stream.scaladsl.Flow
import akka.util.Timeout
import io.circe.syntax._
import test.Protocol._

//#user-routes-class
class WebService(implicit system: ActorSystem) extends Directives {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
  lazy val log = Logging(system, classOf[WebService])

  val theChat: Chat = Chat.create(system)
  import system.dispatcher
  system.scheduler.schedule(15.second, 15.second) {
    theChat.injectMessage(ChatMessage(sender = "clock", s"Bling! The time is ${new Date().toString}."))
  }
  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-routes
  //#users-get-post
  //#users-get-delete
  lazy val routes: Route =
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        // Scala-JS puts them in the root of the resource directory per default,
        // so that's where we pick them up
        path("img")(getFromResourceDirectory("web/img")) ~
        path("css")(getFromResourceDirectory("web/css")) ~
        path("js")(getFromResourceDirectory("web/js")) ~
        path("login")(getFromResource("web/login.html")) ~
        path("ws_api") {
          handleWebSocketMessages(websocketChatFlow())
        }
    }

  def websocketChatFlow(sender: String = "test"): Flow[Message, Message, Any] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) ⇒ msg
      }
      .via(theChat.chatFlow(sender)) // ... and route them through the chatFlow ...
      .map {
        case msg: Protocol.Message ⇒
          TextMessage.Strict(msg.asJson.noSpaces) // ... pack outgoing messages into WS JSON messages ...
      }
      .via(reportErrorsFlow) // ... then log any processing errors on stdin

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          println(s"WS stream failed with $cause")
        case _ => // ignore regular completion
      })
}
