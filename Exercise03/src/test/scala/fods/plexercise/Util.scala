package fods.plexercise

import java.util.concurrent.atomic.AtomicInteger

import fods.plexercise.comlayer.CommunicationLayer
import fods.plexercise.comlayer.impl.{CommunicationLayerSimulator, CommunicationLayerUDP}

import scala.concurrent.ExecutionContextExecutor


object Util {
  def udpComLayer(addr: ProcessAddr) = CommunicationLayerUDP(addr)


  val __port = new AtomicInteger(6100)

  def nextPort: Int = {
    __port.getAndAdd(1)
  }

  def nextLocalHostAddr: ProcessAddr = LocalHost(nextPort)


  def createManagerUDP(p: Int): ChannelManager = {
    val lay = udpComLayer(LocalHost(p)).get
    ChannelManager(lay)
  }

  trait CommunicationLayerTestSession {
    def createLayer(): CommunicationLayer
  }

  class UDPFactory extends CommunicationLayerTestSession {
    override def createLayer(): CommunicationLayer = udpComLayer(LocalHost(nextPort)).get
  }

  class SimulateMSGDropPerDup extends CommunicationLayerTestSession {
    val simulator = new CommunicationLayerSimulator(numBufMessages = 3,
      numRemoveMessages = 2,
      nonDeterministicMSGRemoval = true,
      permutation = true,
      duplication = true)

    override def createLayer(): CommunicationLayer = simulator.createProcessLayer(LocalHost(nextPort))
  }


  class SimulateMSGDrop extends CommunicationLayerTestSession {
    val simulator = new CommunicationLayerSimulator(numBufMessages = 2,
      numRemoveMessages = 1,
      nonDeterministicMSGRemoval = true,
      permutation = false,
      duplication = false)

    override def createLayer(): CommunicationLayer = simulator.createProcessLayer(LocalHost(nextPort))
  }

  class SimulateMSGDuplication extends CommunicationLayerTestSession {
    val simulator = new CommunicationLayerSimulator(numBufMessages = 2,
      numRemoveMessages = 0,
      nonDeterministicMSGRemoval = false,
      permutation = false,
      duplication = true)

    override def createLayer(): CommunicationLayer = simulator.createProcessLayer(LocalHost(nextPort))
  }

  class SimulateMSGPermutation extends CommunicationLayerTestSession {
    val simulator = new CommunicationLayerSimulator(numBufMessages = 3,
      numRemoveMessages = 0,
      nonDeterministicMSGRemoval = false,
      permutation = true,
      duplication = false)

    override def createLayer(): CommunicationLayer = simulator.createProcessLayer(LocalHost(nextPort))
  }

}
