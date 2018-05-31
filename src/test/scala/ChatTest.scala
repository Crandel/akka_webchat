import org.scalatest.{ FunSuite, Matchers }
import chat.WebService
import akka.http.scaladsl.testkit.{ WSProbe, ScalatestRouteTest }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers._

class ChatTest extends FunSuite with Matchers with ScalatestRouteTest {
  val validCredentials = BasicHttpCredentials("admin", "admin")
  val invalidCredentials = BasicHttpCredentials("test user", "some password")

  def assertSocketConnection(credentials: HttpCredentials)(assertions: (WSProbe) => Unit) = {
    val webService = new WebService()
    val wsClient = WSProbe()
    WS("/ws_api", wsClient.flow) ~> addCredentials(credentials) ~> webService.routes ~>
      check(assertions(wsClient))
  }

  test("connect to webserver") {
    assertSocketConnection(validCredentials) { wsClient =>
      isWebSocketUpgrade shouldEqual true
    }
  }

  test("check greetings") {
    assertSocketConnection(validCredentials) { wsClient =>
      wsClient.expectMessage("""{"$type":"joined","member":"admin","allMembers":["admin"]}""")
    }
  }

  test("send test message") {
    assertSocketConnection(validCredentials) { wsClient =>
      wsClient.expectMessage("""{"$type":"joined","member":"admin","allMembers":["admin"]}""")
      wsClient.sendMessage("Hello")
      wsClient.expectMessage("""{"$type":"chat_message","sender":"admin","message":"Hello"}""")
    }
  }
}
