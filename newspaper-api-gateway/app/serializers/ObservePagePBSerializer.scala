package serializers

import java.util

import io.scalac.newspaper.events.{ObservePage}
import org.apache.kafka.common.serialization.Serializer

class ObservePagePBSerializer extends Serializer[ObservePage] {
  override def serialize(topic: String, data: ObservePage): Array[Byte] = {
    ObservePage.messageCompanion.toByteArray(data)
  }

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
  override def close(): Unit = ()
}
