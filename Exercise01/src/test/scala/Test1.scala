import collection.mutable.Stack
import org.scalatest._

class Test1 extends FlatSpec with Matchers {

  "A Server" should "handle multiple clients correctly" in {
    // starting server
    val server = new Server(4701)
    server.start

    // sending terminate
    val client1 = new Client(4711, 4701)
    client1.sendMessage(new Msg("Test1"))

    val client2 = new Client(4712, 4701)
    client2.sendMessage(new Msg("Test2"))

    val result1 = client1.recvMessage()
    val result2 = client2.recvMessage()

    result1 should be ("Test1:echoed")
    result2 should be ("Test2:echoed")
  }
}
