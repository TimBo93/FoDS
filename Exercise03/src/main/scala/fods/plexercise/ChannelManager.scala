package fods.plexercise

import fods.plexercise.comlayer.CommunicationLayer


trait ChannelManager {
  /**
    * The communication layer which needs to be used for all communication with other end points (processes)
    */
  val comLayer: CommunicationLayer

  /** The address of a ChannelManger is the address of the used CommunicationLayer */
  def localAddr: ProcessAddr = comLayer.localAddr

  /**
    * Starts the ChannelManger
    */
  def start(): Unit

  /**
    * Stops all channels which belong to the ChannelManger, the comLayer; and then stops itself
    */
  def stop(): Unit

  /**
    * Establishes a reliable channel between @this.localAddr and the parameter to
    *
    * This call blocks until:
    * * a reliable channel was created successfully
    * * no reliable channel was created after ~ 5 sec  (i.e. timeout of 5 sec)
    *
    * @param to The remote target of the reliable channel
    * @return Either the created channel if successful or None
    */
  def createReliableFifoChannel(to: ProcessAddr): Option[ReliableFifoChannel]

  /**
    * Registers a call back which is called exactly once each time the channel manager established a reliable channel,
    * which was initiated by another channel manger
    *
    * The call back is only called if ReliableFifoChannel was successfully established (i.e. is ready for sending and receiving)
    *
    * @param cb An established ReliableFifoChannel
    */
  def setIncomingConnectionEventCallback(cb: ReliableFifoChannel => Unit)

}

object ChannelManager {

  /**
    * Creates a new ChannelManger
    *
    * @param comLayer The communication layer which the new ChannelManger uses
    * @return new ChannelManger
    */
  //TODO You need to implement this method (The test cases will use this method to create channel managers)
  def apply(comLayer: CommunicationLayer): ChannelManager = ???
}