package fods.plexercise.comlayer.impl

import java.net.{DatagramPacket, DatagramSocket, SocketException}

import com.typesafe.scalalogging.Logger
import fods.plexercise.ProcessAddr
import fods.plexercise.comlayer.{CommunicationLayer, CommunicationLayerMessage}


class CommunicationLayerUDP private(val localAddr: ProcessAddr) extends CommunicationLayer {
  val logger = Logger(classOf[CommunicationLayerUDP])
  val udpSocket = new DatagramSocket(localAddr.port)
  val blockingTime = 10

  override def send(data: CommunicationLayerMessage): Unit = {
    assert(data.source.nonEmpty && data.target.nonEmpty)
    val asArray = data.toByte
    val packet = new DatagramPacket(asArray, asArray.length, data.target.get.iAddr, data.target.get.port)
    try {
      logger.trace(s"UDP layer: snd: ${data}")
      udpSocket.send(packet)
    } catch {
      case e: Throwable => logger.warn(e.getMessage)
    }

  }

  override def receive(): Option[CommunicationLayerMessage] = {
    try {
      val data = new DatagramPacket(new Array[Byte](CommunicationLayerMessage.MAX_BYTE_SIZE), CommunicationLayerMessage.MAX_BYTE_SIZE)
      udpSocket.receive(data)
      val newM = CommunicationLayerMessage.deserialize(data.getData)
      logger.trace(s"UDP layer: received: $newM")
      newM
    } catch {
      case e: SocketException =>
        logger.info(e.getMessage)
        None
      case e: Throwable =>
        logger.warn(e.getMessage)
        None
    }

  }

  override def close(): Unit = udpSocket.close()
}

object CommunicationLayerUDP {

  def apply(localAddr: ProcessAddr): Option[CommunicationLayerUDP] = {
    try {
      Some(new CommunicationLayerUDP(localAddr))
    } catch {
      case e: SocketException =>
        None
    }

  }
}