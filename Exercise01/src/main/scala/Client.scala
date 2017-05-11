import java.net.{DatagramPacket, DatagramSocket, InetAddress}
import com.twitter.chill.KryoInjection

import scala.util.control.NonFatal

/**
  * Created by tborowski on 11.05.2017.
  * @param myPort the local Port to use
  * @param serverPort the port of the server
  */
class Client(myPort: Int, serverPort: Int) {
  var inetAddress = InetAddress.getByName("localhost")
  val mySocket = new DatagramSocket(myPort)


  def sendMessageSafe(msg: MsgType): Unit = {
    var success = false
    while (success == false) {
      msg match {
        case Msg(text: String) => {
          sendMessage(msg)
          val result = recvMessage()
          if (text == result) {
            success = true;
          } else {
            Thread.sleep(100)
          }
        }
        case End() => {
          sendMessage(msg)
          // you can never say, that an end message is correctly sended
          // because you can not verify wheter the message from the server was dropped, or the server is already down
          success = true
        }
        case _ => {
          return
        }
      }
    }
  }

  /**
    * sends a message exactly once
    * @param msg
    */
  def sendMessage(msg: MsgType) {
    val data: Array[Byte] = KryoInjection(msg)
    var packet = new DatagramPacket(data, data.length, inetAddress, serverPort)
    mySocket.send(packet)
  }

  /**
    * receives a message
    * @return Message-String if success, null if not
    */
  def recvMessage(): String =  {
    try {
      val buf = new Array[Byte](256)
      val recvPacket = new DatagramPacket(buf, 0, 256)
      mySocket.receive(recvPacket)

      val IPAddress = recvPacket.getAddress
      val messageReceived = KryoInjection.invert(recvPacket.getData)
      val decodedMessage = messageReceived.getOrElse(null)

      if (decodedMessage == null) {
        return null
      }

      decodedMessage match {
        case Msg(message: String) => {
          System.out.println("Received Message from server: ")
          System.out.println(message)
          return message
        }
        case End() => {
          return null
        }
        case _ => {
          return null
        }
      }
    } catch {
      case NonFatal(t) => {
        return null
      }
    }
  }
}
