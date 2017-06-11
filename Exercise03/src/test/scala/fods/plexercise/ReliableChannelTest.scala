package fods.plexercise


import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

import fods.plexercise.data.TestMessage
import org.scalatest.concurrent.{ScalaFutures, ThreadSignaler, TimeLimitedTests}
import org.scalatest.time.Span
import org.scalatest.time.SpanSugar._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ReliableChannelTest extends FunSuite with ScalaFutures with TimeLimitedTests with BeforeAndAfter {

  import Util._

  val timeLimit: Span = 5.seconds

  override val defaultTestSignaler = ThreadSignaler


  test("Handshake over UDP") {
    handShakeCompletes(new UDPFactory(), numClients = 1)
  }


  test("Handshake with Drop") {
    handShakeCompletes(new SimulateMSGDrop(), numClients = 1)
  }

  test("Handshake with Permutation") {
    handShakeCompletes(new SimulateMSGPermutation(), numClients = 1)
  }

  test("Handshake with Duplication") {
    handShakeCompletes(new SimulateMSGDuplication(), numClients = 1)
  }

  test("Handshake with DropPermDup") {
    handShakeCompletes(new SimulateMSGDropPerDup(), numClients = 1)
  }


  test("Multiple handshakes with UDP") {
    handShakeCompletes(new UDPFactory(), numClients = 3)
  }


  test("Send over UDP is delivered") {
    simpleSend(new UDPFactory)
  }


  test("Send with Drop") {
    simpleSend(new SimulateMSGDrop())
  }

  test("Send with Permutation") {
    simpleSend(new SimulateMSGPermutation())
  }

  test("Send with Duplication") {
    simpleSend(new SimulateMSGDuplication())
  }

  test("Send with DropPermDup") {
    simpleSend(new SimulateMSGDropPerDup())
  }


  test("Send over UDP is received and ordered") {
    simpleSend(new UDPFactory, testOrder = true)
  }

  test("Send with Drop is received and ordered") {
    simpleSend(new SimulateMSGDrop(), testOrder = true)
  }

  test("Send with Permutation is received and ordered") {
    simpleSend(new SimulateMSGPermutation(), testOrder = true)
  }

  test("Send with Duplication is received and ordered") {
    simpleSend(new SimulateMSGDuplication(), testOrder = true)
  }

  test("Send with DropPermDup is received and ordered") {
    simpleSend(new SimulateMSGDropPerDup(), testOrder = true)
  }

  test("Multiple handshakes with Drop") {
    handShakeCompletes(new SimulateMSGDrop(), numClients = 3)
  }

  test("Multiple handshakes Permutation") {
    handShakeCompletes(new SimulateMSGPermutation(), numClients = 3)
  }

  test("Multiple handshakes Duplication") {
    handShakeCompletes(new SimulateMSGDuplication(), numClients = 3)
  }

  test("Multiple handshakes with DropPermDup") {
    handShakeCompletes(new SimulateMSGDropPerDup(), numClients = 3)
  }


  def handShakeCompletes(comLayerFac: CommunicationLayerTestSession, numClients: Int = 2): Unit = {
    val doneSignal = new CountDownLatch(1)
    val server = ChannelManager(comLayerFac.createLayer())
    server.start()
    val numConnected = new AtomicInteger(0)
    val clients = new Array[ProcessAddr](numClients)
    val clientMs = new Array[ChannelManager](numClients)


    server.setIncomingConnectionEventCallback(ch => {
      assert(clients.contains(ch.channel.remoteAddr))
      if (numConnected.addAndGet(1) == numClients) doneSignal.countDown()
    })

    //create all client manager
    for (c <- 1 to numClients) {
      val m = ChannelManager(comLayerFac.createLayer())
      m.start()
      clients(c - 1) = m.localAddr
      clientMs(c - 1) = m
    }

    //create futures which create the channels
    for (c <- 1 to numClients) {
      val m = clientMs(c - 1)
      Future {
        m.createReliableFifoChannel(server.localAddr)
      }
    }


    doneSignal.await()
    clientMs.foreach(_.stop())
    server.stop()
    assert(numConnected.get() == numClients, s"We need: $numConnected.get() == $numClients")
  }


  def simpleSend(comLayerFac: CommunicationLayerTestSession, testOrder: Boolean = false, numMsgs: Int = 10): Unit = {
    val process1 = ChannelManager(comLayerFac.createLayer())
    val process2 = ChannelManager(comLayerFac.createLayer())

    var receivedMsgs = 0
    var expectedID = 1
    val doneSignal = new CountDownLatch(1)
    val lock = new Object()

    val msgText = "test msg"

    process2.setIncomingConnectionEventCallback(newChannel => {
      assert(newChannel.channel.remoteAddr == process1.localAddr)
      newChannel.setReceiveCallback({
        case mt@TestMessage(txt, id) =>
          lock synchronized {
            receivedMsgs += 1
            assert(txt == msgText)
            if (testOrder) assert(id == expectedID)
            expectedID += 1
            if (numMsgs == receivedMsgs) doneSignal.countDown()
          }
      })
    })

    process1.start()
    process2.start()

    val channelToP2 = process1.createReliableFifoChannel(process2.localAddr).get
    for (i <- 1 to numMsgs) {
      channelToP2.send(TestMessage(msgText, i))
    }


    doneSignal.await()
    process1.stop()
    process2.stop()
    assert(receivedMsgs == numMsgs)
  }

}



