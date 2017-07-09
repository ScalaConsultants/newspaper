package io.scalac.inbound

import java.util

import io.scalac.newspaper.events.SubscribeUser
import org.apache.kafka.common.serialization.Deserializer

class SubscribeUserPBDeserializer extends Deserializer[SubscribeUser] {
  override def close(): Unit = ()
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
  override def deserialize(topic: String, data: Array[Byte]): SubscribeUser = {
    SubscribeUser.messageCompanion.parseFrom(data)
  }
}
