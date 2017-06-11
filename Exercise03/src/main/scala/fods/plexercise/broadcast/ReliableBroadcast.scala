package fods.plexercise.broadcast

import fods.plexercise.{Message, ProcessAddr, ReliableFifoChannel}

/**
  * A reliable broadcast with the following properties:
  * - I. No duplication (See Broadcast slides page 11)
  * - II. No Creation (See Broadcast slides page 11)
  * - III. Validity: If p and q are correct, then every message broadcasted by p is eventually delivered by q (See Broadcast slides page 11)
  * - IV. Agreement: If one correct process delivers a message m, every correct process eventually delivers m. (See Broadcast slides page 11)
  *
  */
trait ReliableBroadcast {
  /**
    * Broadcasts m to all processes connected via reliable channels added by addChannel
    * This call is non-blocking
    *
    * @param m Broadcast message
    */
  def broadcast(m: Message): Unit

  /**
    * Set a callback for message delivery.
    *
    * Behaviour is undefined if no callback is registered and the broadcast wants to deliver a message
    *
    * @param cb The delivery call back
    */
  def setDeliverCallback(cb: Message => Unit): Unit


  /**
    * Adds a channel which should be used for broadcast
    *
    * @param channel Added reliable channel
    */
  def addChannel(channel: ReliableFifoChannel): Unit

}

object ReliableBroadcast {

  //TODO update this method so that it creates an instance of ReliableBroadcast
  def apply(lAddr: ProcessAddr, channel: List[ReliableFifoChannel] = Nil): ReliableBroadcast = {
    val rb: ReliableBroadcast = ???
    for (c <- channel) {
      rb.addChannel(c)
    }
    rb
  }

}