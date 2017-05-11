import collection.mutable.Stack
import org.scalatest._

class Test2 extends FlatSpec with Matchers {

  "A Server" should "reply correctly" in {
    // starting server
    val server = new Server(4702)
    server.start

    // sending terminate
    val client = new Client(4721, 4702)
    client.sendMessage(new Msg("Test"))
    val result = client.recvMessage()

    result should be ("Test:echoed")
  }
}
