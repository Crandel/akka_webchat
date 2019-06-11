package chat
import io.circe._

object Protocol {
  private val left = "left"
  private val chat_message = "chat_message"
  private val joined = "joined"

  sealed trait Message

  implicit val encodeMessage: Encoder[Message] = new Encoder[Message] {

    final def apply(a: Message): Json = {
      a match {
        case cm: ChatMessage => Json.obj(
          ("$type", Json.fromString(chat_message)),
          ("sender", Json.fromString(cm.sender)),
          ("message", Json.fromString(cm.message)))

        case j: Joined =>
          Json.obj(
            ("$type", Json.fromString(joined)),
            ("member", Json.fromString(j.member)),
            ("allMembers", Json.fromValues(j.allMembers.map(member => Json.fromString(member)))))
        case l: LeftMessage =>
          Json.obj(
            ("$type", Json.fromString(left)),
            ("member", Json.fromString(l.member)),
            ("allMembers", Json.fromValues(l.allMembers.map(member => Json.fromString(member)))))

      }

    }

    implicit val decodeMessage: Decoder[Message] = new Decoder[Message] {
      final def apply(c: HCursor): Decoder.Result[Message] = {
        for {
          m_type <- c.downField("$type").as[String]
        } yield {
          val result = m_type match {
            case `chat_message` => for {
              username <- c.downField("sender").as[String]
              password <- c.downField("message").as[String]
            } yield ChatMessage(username, password)

            case `joined` => for {
              member <- c.downField("member").as[String]
              allMembers <- c.downField("allMembers").as[Seq[String]]
            } yield Joined(member, allMembers)

            case `left` => for {
              member <- c.downField("member").as[String]
              allMembers <- c.downField("allMembers").as[Seq[String]]
            } yield LeftMessage(member, allMembers)
          }
          result match {
            case Right(msg) => msg
          }
        }
      }
    }
  }
  case class ChatMessage(sender: String, message: String) extends Message

  case class Joined(member: String, allMembers: Seq[String]) extends Message

  case class LeftMessage(member: String, allMembers: Seq[String]) extends Message
}
