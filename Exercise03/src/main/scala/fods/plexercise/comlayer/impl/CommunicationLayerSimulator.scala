package fods.plexercise.comlayer.impl

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import com.typesafe.scalalogging.Logger
import fods.plexercise.ProcessAddr
import fods.plexercise.comlayer.{CommunicationLayer, CommunicationLayerMessage, CommunicationLayerSession}

import scala.collection.concurrent.TrieMap
import scala.util.Random


class CommunicationLayerSimulator(
                                   numBufMessages: Int = 3, //number of msg which are buffered between two endpoints before delivery
                                   numRemoveMessages: Int = 0, //number of msg which will be removed from the buffered msgs
                                   nonDeterministicMSGRemoval: Boolean = true, // if true => we remove Random.nextInt(numRemoveMessages) msgs
                                   permutation: Boolean = false, //if true messages will be permuted before delivery
                                   duplication: Boolean = false // if true msg can duplicated
                                 ) extends CommunicationLayerSession {

  val logger = Logger(classOf[CommunicationLayerSimulator])
  val lock = new Object


  val channels: TrieMap[(ProcessAddr, ProcessAddr), List[CommunicationLayerMessage]] = TrieMap[(ProcessAddr, ProcessAddr), List[CommunicationLayerMessage]]()
  val msgInDelivery: TrieMap[ProcessAddr, LinkedBlockingQueue[CommunicationLayerMessage]] = TrieMap[ProcessAddr, LinkedBlockingQueue[CommunicationLayerMessage]]()
  val endPoints: TrieMap[ProcessAddr, CommunicationLayerSimulatorEndPoint] = TrieMap[ProcessAddr, CommunicationLayerSimulatorEndPoint]()

  var brokenLinks: Set[(ProcessAddr, ProcessAddr)] = Set[(ProcessAddr, ProcessAddr)]()


  override def createProcessLayer(from: ProcessAddr): CommunicationLayer = {
    val res = new CommunicationLayerSimulatorEndPoint(this, from)
    endPoints += from -> res
    res
  }

  /**
    * Sends a message m : CommunicationLayerMessage
    *
    * The semantic depends on the configuration of this simulator
    *
    */
  def send(m: CommunicationLayerMessage): Unit = {
    lock synchronized {

      (m.source, m.target) match {
        case (Some(f), Some(t)) =>

          val cKey = (f, t)
          if (brokenLinks.contains((f, t)))
            return

          var msgInTransit = channels.getOrElseUpdate(cKey, List()) :+ m
          val unchanged = msgInTransit

          if (msgInTransit.size >= numBufMessages) {

            if (permutation) {
              val perm = msgInTransit.permutations.toArray
              msgInTransit = perm(Random.nextInt(Math.max(1, perm.length - 1)))
            }

            if (duplication) {
              msgInTransit = msgInTransit.last :: msgInTransit
            }

            val nMsgRemove = if (nonDeterministicMSGRemoval && numRemoveMessages > 0) Random.nextInt(numRemoveMessages) else numRemoveMessages

            val toSend = msgInTransit.drop(nMsgRemove)

            logger.trace(s"[Simulator] Delivers: $toSend [$f -> $t](The following where initially send: $unchanged)")

            toSend.foreach(m => {
              val l = msgInDelivery.getOrElseUpdate(t, new LinkedBlockingQueue[CommunicationLayerMessage]())
              l.put(m)
            })

            channels += (cKey -> Nil)
          } else {
            logger.trace(s"[Simulator] Send was called with $m (${m.source} -> ${m.target} \nNO MSG delivery")
            channels += (cKey -> msgInTransit)
          }
        case _ =>
          assert(false)
      }
    }

  }


}

class CommunicationLayerSimulatorEndPoint(val manager: CommunicationLayerSimulator, val localAddr: ProcessAddr) extends CommunicationLayer {
  val logger = Logger(classOf[CommunicationLayerSimulatorEndPoint])


  var closed = false

  override def send(m: CommunicationLayerMessage): Unit = {
    assert(m.source.nonEmpty && m.target.nonEmpty)
    manager.send(m)
  }

  override def receive(): Option[CommunicationLayerMessage] = {
    while (!closed) {
      try {
        val m = manager.msgInDelivery.getOrElseUpdate(localAddr, new LinkedBlockingQueue[CommunicationLayerMessage]()).poll(100, TimeUnit.MILLISECONDS)
        if (m != null) {
          return Some(m)
        }

      } catch {
        case e: InterruptedException =>
          logger.trace(s"ComLayerLossyPermutationManager was interrupted")
        case e: Throwable =>
          logger.warn(s"Throwable: ${e.printStackTrace()}")
          None
      }
    }
    None
  }

  override def close(): Unit = {
    closed = true
  }
}
