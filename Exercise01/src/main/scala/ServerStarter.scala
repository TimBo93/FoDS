import java.net.{DatagramPacket, DatagramSocket}

import com.twitter.chill.KryoInjection

/**
  * Created by tborowski on 11.05.2017.
  */
object ServerStarter {
  def main(args: Array[String]) {
    val server = new Server(4700)
    server.start
  }
}
