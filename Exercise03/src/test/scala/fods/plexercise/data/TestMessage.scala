package fods.plexercise.data

import fods.plexercise.Message


case class TestMessage(data: String, orderTest: Int) extends Message

case class ATextMessage(txt: String) extends Message
