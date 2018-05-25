package test
import io.circe._
import io.circe.generic.semiauto._

object Protocol {
  sealed trait Message
  case class ChatMessage(sender: String, message: String) extends Message
  implicit val messageDecoder: Decoder[Message] = deriveDecoder
  implicit val messageEncoder: Encoder[Message] = deriveEncoder
  case class Joined(member: String, allMembers: Seq[String]) extends Message
  case class Left(member: String, allMembers: Seq[String]) extends Message
}
