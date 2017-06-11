package fods.plexercise

import java.util.concurrent.CountDownLatch

import fods.plexercise.comlayer.SemiReliableCommunicationLayerSession
import fods.plexercise.data.ATextMessage
import org.scalatest.FunSuite
import org.scalatest.concurrent.{ScalaFutures, ThreadSignaler, TimeLimitedTests}
import org.scalatest.time.Span
import org.scalatest.time.SpanSugar._

class ReliableChannelOverSemiReliableChannel extends FunSuite with ScalaFutures with TimeLimitedTests {

  import Util._

  val timeLimit: Span = 50000.seconds

  override val defaultTestSignaler = ThreadSignaler


  test("Send over Semi Reliable Channel is Received") {
    val doneSignal = new CountDownLatch(1)

    val comLayerFac = SemiReliableCommunicationLayerSession.createSemiReliableSession()
    val a = ChannelManagerClassExercise(comLayerFac.createProcessLayer(nextLocalHostAddr))
    val b = ChannelManagerClassExercise(comLayerFac.createProcessLayer(nextLocalHostAddr))
    a.start()
    b.start()
    val msgText = "the msg"

    b.setIncomingConnectionEventCallback(cB => {
      cB.setReceiveCallback {
        case ATextMessage(txt) if txt == msgText =>
          doneSignal.countDown()
      }
    })

    val cA = a.createReliableFifoChannel(b.localAddr)
    cA.send(ATextMessage(msgText))
    doneSignal.await()
    a.stop()
    b.stop()
  }


  test("Bidirectional Send over Semi Reliable Channel is Received") {
    val doneSignal = new CountDownLatch(1)

    val comLayerFac = SemiReliableCommunicationLayerSession.createSemiReliableSession()
    val a = ChannelManagerClassExercise(comLayerFac.createProcessLayer(nextLocalHostAddr))
    val b = ChannelManagerClassExercise(comLayerFac.createProcessLayer(nextLocalHostAddr))
    a.start()
    b.start()
    val msgText = "the msg"
    val msgText2 = "the msg2"

    b.setIncomingConnectionEventCallback(cB => {
      cB.setReceiveCallback {
        case ATextMessage(txt) if txt == msgText =>
          cB.send(ATextMessage(msgText2))
      }
    })

    val cA = a.createReliableFifoChannel(b.localAddr)

    cA.setReceiveCallback({
      case ATextMessage(txt)
        if txt == msgText2 =>
        doneSignal.countDown()
    })

    cA.send(ATextMessage(msgText))
    doneSignal.await()
    a.stop()
    b.stop()
  }


}
