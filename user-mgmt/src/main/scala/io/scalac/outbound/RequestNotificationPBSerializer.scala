package io.scalac.outbound

import java.util

import io.scalac.newspaper.events.RequestNotification
import org.apache.kafka.common.serialization.Serializer

class RequestNotificationPBSerializer extends Serializer[RequestNotification] {

  override def serialize(topic: String, data: RequestNotification): Array[Byte] = {
    RequestNotification.messageCompanion.toByteArray(data)
  }

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()
  override def close(): Unit = ()
}
