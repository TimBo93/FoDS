import java.net._

import com.twitter.chill.KryoInjection

/**
  * Created by tborowski on 09.05.2017.
  */
class Server(port: Int) {
  var isRunning = false;


  val thread = new Thread {
    val recvSocket = new DatagramSocket(port)

    override def run(): Unit = {
      while(true){
        val buf = new Array[Byte](256)
        val recvPacket = new DatagramPacket(buf, 0, 256)
        recvSocket.receive(recvPacket)
        val messageReceived  = KryoInjection.invert(recvPacket.getData)
        val decodedMessage = messageReceived.getOrElse(null)
        decodedMessage match {
          case Msg(message: String) => {
            System.out.println("answering to " + recvPacket.getAddress + recvPacket.getPort)
            val messageEchoed = new Msg(message + ":echoed")
            val dataToSend = KryoInjection(messageEchoed)
            val sendPacket = new DatagramPacket(dataToSend, dataToSend.length, recvPacket.getAddress, recvPacket.getPort)
            recvSocket.send(sendPacket)
          }
          case End() => {
            // Notice
            // it does not make any sense to respond to this message
            // because the response message could also get "lost"
            System.out.println("shutting server down " + recvPacket.getAddress + recvPacket.getPort)
            recvSocket.close
            return
          }
          case _ => {
          }
        }
      }
    }
  }

  def isStarted(): Boolean = {
    return thread.isAlive
  }

  def start(): Unit = {
    thread.start()
  }
}
