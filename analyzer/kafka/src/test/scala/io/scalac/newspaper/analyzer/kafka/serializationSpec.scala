package io.scalac.newspaper.analyzer.kafka

import org.scalatest._

import io.scalac.newspaper.events._

class serializationSpec extends FlatSpec with Matchers {

  "ContentFetched" should "be serialized and deserialized properly" in {
    val contentFetched = ContentFetched("foo", "bar")
    val topic = "fnord"
    val serializer = new ContentFetchedSerializer
    val deserializer = new ContentFetchedDeserializer
    deserializer.deserialize(topic, serializer.serialize(topic, contentFetched)) shouldEqual contentFetched
  }

  "ChangeDetected" should "be serialized and deserialized properly" in {
    val changeDetected = ChangeDetected("foo", "bar")
    val topic = "fnord"
    val serializer = new ChangeDetectedSerializer
    val deserializer = new ChangeDetectedDeserializer
    deserializer.deserialize(topic, serializer.serialize(topic, changeDetected)) shouldEqual changeDetected
  }

}
