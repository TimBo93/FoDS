package fods.plexercise

import fods.plexercise.comlayer.CommunicationLayer


trait ChannelManagerClassExercise {
  /**
    * The communication layer which needs to be used for all communication with other end points (processes)
    */
  val comLayer: CommunicationLayer

  /** The address of a ChannelManagerClassExercise is the address of the used CommunicationLayer */
  def localAddr: ProcessAddr = comLayer.localAddr

  /**
    * Starts the ChannelManagerClassExercise
    */
  def start(): Unit

  /**
    * Stops all channels which belong to the ChannelManagerClassExercise, the comLayer and then stop itself
    */
  def stop(): Unit

  /**
    * Creates a reliable channel between @this.localAddr and the parameter to
    *
    * @param to The remote target of the reliable channel
    * @return The created channel
    */
  def createReliableFifoChannel(to: ProcessAddr): ReliableFifoChannel

  /**
    * Registers a call back which is called exactly once each time the channel manager established a reliable channel,
    * which was initiated by another channel manger
    *
    * @param cb An established ReliableFifoChannel
    */
  def setIncomingConnectionEventCallback(cb: ReliableFifoChannel => Unit)

}

object ChannelManagerClassExercise {
  /**
    * Creates a new ChannelManagerClassExercise
    *
    * @param comLayer A communication layer which is semi reliable (see Problem 3.1 for further details)
    * @return new ChannelManagerClassExercise
    */
  //TODO Implement
  def apply(comLayer: CommunicationLayer): ChannelManagerClassExercise = ???
}