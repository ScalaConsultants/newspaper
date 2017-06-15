package io.scalac.newspaper.analyzer

import java.util
import org.apache.kafka.common.serialization.Deserializer

import events._

class ContentFetchedPBDeserializer() extends Deserializer[ContentFetched] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()

  override def deserialize(topic: String, data: Array[Byte]): ContentFetched = {
    ContentFetched.messageCompanion.parseFrom(data)
  }

}
