package chat

import java.util.Date

import scala.collection.mutable
import scala.util.Failure
import scala.concurrent.duration._
import akka.actor._
import akka.event.Logging
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.scaladsl.Flow
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import chat.Protocol._

case class User(name: String, password: String)

object User {
  implicit val userEncoder: Encoder[User] = deriveEncoder
  implicit val userDecoder: Decoder[User] = deriveDecoder
}

class WebService(implicit system: ActorSystem) extends Directives {

  lazy val log = Logging(system, classOf[WebService])

  val theChat: Chat = Chat.create(system)

  import system.dispatcher

  system.scheduler.schedule(60.second, 60.second) {
    theChat.injectMessage(ChatMessage(sender = "clock", s"Bling! The time is ${new Date().toString}."))
  }

  def createToken(user: User): String = {
    BasicHttpCredentials(user.name, user.password).token
  }

  def checkUser(username: String): Boolean = {
    session.contains(username)
  }

  def myUserPassAuthenticator(credentials: Credentials): Option[String] =
    credentials match {
      case p @ Credentials.Provided(id) =>
        if (p.verify("admin")) Some(id) else None
      case _ => None
    }

  val admin = User("admin", "admin")

  val session = mutable.Map(admin.name -> createToken(admin))

  def login(user: User): String = {
    if (!session.contains(user.name)) {
      val token = createToken(user)
      session += (user.name -> token)
      token
    } else {
      session(user.name)
    }
  }

  lazy val routes: Route =
    get {
      authenticateBasic(realm = "secure site", myUserPassAuthenticator) { user =>
        pathSingleSlash {
          getFromResource("web/index.html")
        } ~
          path("ws_api") {
            handleWebSocketMessages(websocketChatFlow(user.name))
          }
      }
    } ~
      getFromResourceDirectory("web")

  def websocketChatFlow(sender: String = "test"): Flow[Message, Message, Any] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(msg) ⇒ msg
      }
      .via(theChat.chatFlow(sender))
      .map {
        case msg: Protocol.Message ⇒
          TextMessage.Strict(msg.asJson.noSpaces)
      }
      .via(reportErrorsFlow)

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          println(s"WS stream failed with $cause")
        case _ =>
      })
}
