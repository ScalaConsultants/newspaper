package io.scalac.inbound

import java.util

import io.scalac.newspaper.events._
import org.apache.kafka.common.serialization.Deserializer

case class ChangeDetectedPBDeserializer() extends Deserializer[ChangeDetected] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = () // nothing to do

  override def close(): Unit = () // nothing to do

  override def deserialize(topic: String, data: Array[Byte]): ChangeDetected = {
    //    ChangeDetected.apply("foo", "bar") //in case of poisonous messages stored on kafka
    ChangeDetected.messageCompanion.parseFrom(data)
  }

}