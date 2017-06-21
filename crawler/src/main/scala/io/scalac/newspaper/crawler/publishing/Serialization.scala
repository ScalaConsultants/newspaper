package io.scalac.newspaper.crawler.publishing


import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
import java.util

import org.apache.kafka.common.serialization.{Deserializer, Serializer}
import events._

class ProtobufSerializer[T <: GeneratedMessage with Message[T]](messageCompanion: GeneratedMessageCompanion[T]) extends Serializer[T] {

 override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()

  override def serialize(topic: String, event: T): Array[Byte] = {
        messageCompanion.toByteArray(event)
      }

    }

class ProtobufDeserializer[T <: GeneratedMessage with Message[T]](messageCompanion: GeneratedMessageCompanion[T]) extends Deserializer[T] {

      override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

      override def close(): Unit = ()

      override def deserialize(topic: String, data: Array[Byte]): T = {
        messageCompanion.parseFrom(data)
      }

    }

class ContentFetchedSerializer extends ProtobufSerializer(ContentFetched)
class ContentFetchedDeserializer extends ProtobufDeserializer(ContentFetched)