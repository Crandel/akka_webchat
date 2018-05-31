import org.scalatest.{ FunSuite, Matchers }
import chat.WebService
import akka.http.scaladsl.testkit.{ WSProbe, ScalatestRouteTest }
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.model.headers._

class ChatTest extends FunSuite with Matchers with ScalatestRouteTest {
  val webService = new WebService()
  val wsClient = WSProbe()

  val validCredentials = BasicHttpCredentials("admin", "admin")
  test("connect to webserver") {
    WS("/ws_api", wsClient.flow) ~> addCredentials(validCredentials) ~> webService.routes ~>
      check {
        isWebSocketUpgrade shouldEqual true
      }
  }
}
