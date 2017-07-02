package io.scalac.newspaper.mailer.inbound

import java.util

import io.scalac.newspaper.events._
import org.apache.kafka.common.serialization.Deserializer

case class RequestNotificationPBDeserializer() extends Deserializer[RequestNotification] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = () // nothing to do

  override def close(): Unit = () // nothing to do

  override def deserialize(topic: String, data: Array[Byte]): RequestNotification = {
    RequestNotification.messageCompanion.parseFrom(data)
  }

}
