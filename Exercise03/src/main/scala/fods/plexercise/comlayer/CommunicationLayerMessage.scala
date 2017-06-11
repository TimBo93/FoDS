package fods.plexercise.comlayer

import com.twitter.chill.KryoInjection
import com.typesafe.scalalogging.Logger
import fods.plexercise.ProcessAddr

import scala.util.Success


trait CommunicationLayerMessage extends Serializable {

  /** Serializes the message */
  def toByte: Array[Byte] = KryoInjection(this)

  /** The address of the sender of this message */
  def source: Option[ProcessAddr]

  /** The destination of this message */
  def target: Option[ProcessAddr]


  /** CommunicationLayerMessage needs to be serializable and smaller then MAX_BYTE_SIZE */
  assert(toByte.length > 0 && toByte.length < CommunicationLayerMessage.MAX_BYTE_SIZE)

}


object CommunicationLayerMessage {
  val logger = Logger(classOf[CommunicationLayerMessage])
  val MAX_BYTE_SIZE = 1024

  def deserialize(data: Array[Byte]): Option[CommunicationLayerMessage] =
    KryoInjection.invert(data) match {
      case Success(msg: CommunicationLayerMessage) =>
        Some(msg)
      case d@_ =>
        logger.warn(s"Deserialization failed")
        None
    }

}