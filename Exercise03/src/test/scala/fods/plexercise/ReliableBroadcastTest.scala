package fods.plexercise


import fods.plexercise.broadcast.ReliableBroadcast
import fods.plexercise.data.TestMessage
import org.scalatest.concurrent.{ScalaFutures, ThreadSignaler, TimeLimitedTests}
import org.scalatest.time.Span
import org.scalatest.time.SpanSugar._
import org.scalatest.{BeforeAndAfter, FunSuite}

class ReliableBroadcastTest extends FunSuite with ScalaFutures with TimeLimitedTests with BeforeAndAfter {

  import Util._

  val timeLimit: Span = 5.seconds

  override val defaultTestSignaler = ThreadSignaler


  test("Reliable BCast over UDP") {
    simpleBCast(new UDPFactory, 3)
  }
  test("Reliable BCast in Simulator(Drop Per & Dup)") {
    simpleBCast(new SimulateMSGDropPerDup(), 3)
  }


  test("Reliable BCast in Simulator(Drop Per & Dup) not Completely Connected") {
    bCastBrokenLinks(new SimulateMSGDropPerDup(), 4)
  }


  test("Multiple Reliable BCast over UDP") {
    multipleBCast(new UDPFactory, 3)
  }
  test("Multiple Reliable BCast in Simulator(Drop Per & Dup)") {
    multipleBCast(new SimulateMSGDropPerDup(), 3)
  }


  def bCastBrokenLinks(comLayerFac: SimulateMSGDropPerDup, numClients: Int = 3): Unit = {
    val maRBC = createComMangerAndSetup(comLayerFac, numClients)
    val lock = new Object()
    val testM = TestMessage("hello", 9)
    var processWhichDeliveredBCastM = Set[ProcessAddr]()

    val sE: ProcessAddr = maRBC(0)._1.localAddr
    val cE = maRBC(1)._1.localAddr

    //this list is for simulating non working links
    val bLinks = maRBC.flatMap(t => {
      val e: ProcessAddr = t._1.localAddr
      if (e == sE || e == cE)
        List()
      else
        List((sE, e), (e, sE))
    }).toSet
    comLayerFac.simulator.brokenLinks = bLinks

    for (i <- 1 until numClients) {
      maRBC(i)._2.setDeliverCallback({
        case msg@TestMessage(data, ord) =>
          assert(msg == testM)
          lock synchronized {
            assert(!processWhichDeliveredBCastM.contains(maRBC(i)._1.localAddr))
            processWhichDeliveredBCastM = processWhichDeliveredBCastM + maRBC(i)._1.localAddr
          }

      })
    }

    maRBC(0)._2.broadcast(testM)

    while (processWhichDeliveredBCastM.size < numClients - 1) {
      Thread.sleep(100)
    }

  }


  def simpleBCast(comLayerFac: CommunicationLayerTestSession, numClients: Int = 2, killLink: Boolean = false): Unit = {
    val a = createComMangerAndSetup(comLayerFac, numClients)
    val lock = new Object()
    val testM = TestMessage("hello", 9)
    var endPWhichDeliveredBCast = Set[ProcessAddr]()


    for (i <- 1 until numClients) {
      a(i)._2.setDeliverCallback({
        case msg@TestMessage(data, ord) =>
          assert(msg == testM)
          lock synchronized {
            endPWhichDeliveredBCast = endPWhichDeliveredBCast + a(i)._1.localAddr
          }

      })
    }
    a(0)._2.broadcast(testM)

    while (endPWhichDeliveredBCast.size < numClients - 1) {
      Thread.sleep(100)
    }

  }


  def multipleBCast(comLayerFac: CommunicationLayerTestSession, numClients: Int = 2): Unit = {
    val maBCast = createComMangerAndSetup(comLayerFac, numClients)
    val lock = new Object()
    val testM = "hello"

    var msgCounter = Map[Int, Int]()

    for (i <- 0 until numClients) {
      maBCast(i)._2.setDeliverCallback({
        case msg@TestMessage(data, ord) =>
          assert(data == testM)
          lock synchronized {
            msgCounter = msgCounter + (ord -> (msgCounter.getOrElse(ord, 0) + 1))
          }

      })
    }

    for (i <- 0 until numClients) {
      maBCast(i)._2.broadcast(TestMessage(testM, maBCast(i)._1.localAddr.port))
    }

    while (msgCounter.size != numClients || msgCounter.values.exists(_ != numClients - 1)) {
      Thread.sleep(100)
    }

  }

  /**
    * Create numClient ChannelManagers and connect all ChannelManger with each other by reliable channels
    * For every ChannelManager create one ReliableBroadcast
    **/
  def createComMangerAndSetup(comLayerFac: CommunicationLayerTestSession, numClients: Int = 2): Array[(ChannelManager, ReliableBroadcast)] = {
    val manager = new Array[ChannelManager](numClients)
    val managerChannels = Array.fill[List[ReliableFifoChannel]](numClients)(Nil)

    for (i <- 0 until numClients) {
      manager(i) = ChannelManager(comLayerFac.createLayer())
      manager(i).start()
      manager(i).setIncomingConnectionEventCallback(c => {
        managerChannels synchronized {
          managerChannels(i) = c :: managerChannels(i)
        }
      })
    }

    for (i <- 0 until numClients) {
      for (j <- i + 1 until numClients) {
        manager(i).createReliableFifoChannel(LocalHost(manager(j).localAddr.port)) match {
          case Some(c) =>
            managerChannels synchronized {
              managerChannels(i) = c :: managerChannels(i)
            }
          case _ => assert(false)
        }

      }
    }

    var done = false

    while (!done) {
      Thread.sleep(100)
      done = true
      managerChannels.foreach(cs => {
        done = done && (cs.size == numClients - 1)
      })
    }

    val res = new Array[(ChannelManager, ReliableBroadcast)](numClients)
    for (i <- 0 until numClients) {
      val rb = ReliableBroadcast(manager(i).localAddr, managerChannels(i))
      res(i) = (manager(i), rb)
    }
    res
  }


}
