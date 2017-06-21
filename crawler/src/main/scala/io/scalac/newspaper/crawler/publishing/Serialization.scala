package io.scalac.newspaper.crawler.publishing


import java.util

import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
import io.scalac.newspaper.events.ContentFetched
import org.apache.kafka.common.serialization.Serializer

class ProtobufSerializer[T <: GeneratedMessage with Message[T]](messageCompanion: GeneratedMessageCompanion[T]) extends Serializer[T] {

 override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()

  override def serialize(topic: String, event: T): Array[Byte] = {
        messageCompanion.toByteArray(event)
      }

    }

class ContentFetchedSerializer extends ProtobufSerializer(ContentFetched)