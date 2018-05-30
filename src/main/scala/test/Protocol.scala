package test
import io.circe._
import io.circe.generic.semiauto._

object Protocol {
  sealed trait Message
  implicit val messageDecoder: Decoder[Message] = deriveDecoder
  implicit val messageEncoder: Encoder[Message] = deriveEncoder

  case class ChatMessage(sender: String, message: String) extends Message
  implicit val chatDecoder: Decoder[ChatMessage] = deriveDecoder
  implicit val chatEncoder: Encoder[ChatMessage] = deriveEncoder

  case class Joined(member: String, allMembers: Seq[String]) extends Message
  implicit val joinedDecoder: Decoder[Joined] = deriveDecoder
  implicit val joinedEncoder: Encoder[Joined] = deriveEncoder

  case class Left(member: String, allMembers: Seq[String]) extends Message
  implicit val leftDecoder: Decoder[Left] = deriveDecoder
  implicit val leftEncoder: Encoder[Left] = deriveEncoder

}
