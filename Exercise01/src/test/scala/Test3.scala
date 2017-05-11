import collection.mutable.Stack
import org.scalatest._

class Test3 extends FlatSpec with Matchers {

  "A Server" should "terminate after getting end" in {
    // starting server
    val server = new Server(4703)
    server.start

    // sending terminate
    val client = new Client(4731, 4703)
    client.sendMessage(new End)
    Thread.sleep(2000)
    server.isStarted should be (false)
  }
}
