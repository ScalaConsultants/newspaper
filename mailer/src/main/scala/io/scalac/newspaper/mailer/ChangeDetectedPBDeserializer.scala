package io.scalac.newspaper.mailer

import java.util
import org.apache.kafka.common.serialization.Deserializer
import io.scalac.newspaper.mailer.events._

case class ChangeDetectedPBDeserializer() extends Deserializer[ChangeDetected] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = () // nothing to do

  override def close(): Unit = () // nothing to do

  override def deserialize(topic: String, data: Array[Byte]): ChangeDetected = {
//    ChangeDetected.apply("foo", "bar", "baz") //TODO: hard to say if it works :)
    ChangeDetected.messageCompanion.parseFrom(data)
  }

}
