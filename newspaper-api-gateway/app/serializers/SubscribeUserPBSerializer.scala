package serializers

import java.util

import io.scalac.newspaper.events.SubscribeUser
import org.apache.kafka.common.serialization.Serializer

class SubscribeUserPBSerializer extends Serializer[SubscribeUser] {
  override def serialize(topic: String, data: SubscribeUser): Array[Byte] = {
    SubscribeUser.messageCompanion.toByteArray(data)
  }

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
  override def close(): Unit = ()
}
