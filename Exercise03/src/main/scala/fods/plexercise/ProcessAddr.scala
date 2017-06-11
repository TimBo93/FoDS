package fods.plexercise

import java.net.InetAddress

/**
  * @param ipAddr The ip address of the process (end point) as string. (It needs to be processable by InetAddress.getByName(String)
  * @param port   The port of the processes (end point)
  */
case class ProcessAddr(ipAddr: String, port: Int) {
  assert(InetAddress.getByName(ipAddr) != null)

  def iAddr: InetAddress = InetAddress.getByName(ipAddr)
}

object ProcessAddr {
  def apply(ip: String, port: Int): ProcessAddr = new ProcessAddr(ip, port)
}

object LocalHost {
  def apply(port: Int): ProcessAddr = ProcessAddr("127.0.0.1", port)
}