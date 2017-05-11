import java.net.{DatagramPacket, DatagramSocket, InetAddress}

import com.twitter.chill.KryoInjection

import scala.util.control.NonFatal

/**
  * Created by tborowski on 04.05.2017.
  */
object ClientStarter {
  def main(args: Array[String]) {
    val client = new Client(4711, 4700)
    for (i <- 1 to 10) {
        client.sendMessage(new Msg("Nachricht # " + i))
    }
    client.sendMessage(new End)
  }
}
