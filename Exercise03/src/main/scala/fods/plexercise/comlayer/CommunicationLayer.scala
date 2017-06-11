package fods.plexercise.comlayer

import fods.plexercise.ProcessAddr
import fods.plexercise.comlayer.impl.CommunicationLayerSimulator

/**
  * Connectionless communication layer for send and receiving CommunicationLayerMessage
  */
trait CommunicationLayer {

  /** The address of this communication layer (and of the process which uses this communication later */
  def localAddr: ProcessAddr

  /**
    * Uses a send-and-forget semantic.
    * If an error occurs during sending
    *   - no exception is thrown
    *   - it is reported to a logger framework
    */
  def send(m: CommunicationLayerMessage)


  /**
    * Blocking call
    */
  def receive(): Option[CommunicationLayerMessage]

  /**
    * Closes the communication layer
    */
  def close(): Unit
}

/**
  * A CommunicationLayerSession connects CommunicationLayer into one session
  * This is only used for local testing
  */
trait CommunicationLayerSession {
  def createProcessLayer(addr: ProcessAddr): CommunicationLayer
}


class SemiReliableCommunicationLayerSession extends CommunicationLayerSimulator(
  numBufMessages = 2,
  numRemoveMessages = 1,
  nonDeterministicMSGRemoval = false,
  permutation = false,
  duplication = false) {
}


/**
  * Create a communication layer which for point to point connection provides the following properties:
  *   - No creation
  *   - No duplication
  *   - (Semi) Validity: Every seconded message that a correct process p sends to a correct process q is eventually received by q
  */
object SemiReliableCommunicationLayerSession {
  def createSemiReliableSession(): SemiReliableCommunicationLayerSession = new SemiReliableCommunicationLayerSession()
}
