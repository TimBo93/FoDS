abstract sealed class MsgType
case class Msg(message: String) extends MsgType
case class End() extends MsgType
