package io.scalac.newspaper.analyzer

import java.util
import org.apache.kafka.common.serialization.Serializer

import events._

class ChangeDetectedPBSerializer() extends Serializer[ChangeDetected] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()

  override def serialize(topic: String, event: ChangeDetected): Array[Byte] = {
    ChangeDetected.messageCompanion.toByteArray(event)
  }

}
