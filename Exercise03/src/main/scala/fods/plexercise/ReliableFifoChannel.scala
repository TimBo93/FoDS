package fods.plexercise

import fods.plexercise.data.Channel

/**
  * A reliable channel with the following properties
  *   - I. No duplication (see Unicast slides page 4)
  *   - II. No creation (see Unicast slides page 4)
  *   - III. Validity (see Unicast slides page 5)
  *   - IV. FiFo order receives:
  * If a process p sends a massage m before a message m' to a process q then q receives m before m'
  */
trait ReliableFifoChannel {
  /**
    * @return The channel data (local address , remote address)
    */
  def channel: Channel

  /**
    * Sends the message m.
    * This method is non-blocking
    *
    * @param m the sent message
    */
  def send(m: Message): Unit

  /**
    * Registers a callback for message receive.
    *
    * If the channel receives a message and no callback is registered, then the behaviour is undefined
    *
    * @param cb The delivery call back
    */
  def setReceiveCallback(cb: Message => Unit): Unit

}
