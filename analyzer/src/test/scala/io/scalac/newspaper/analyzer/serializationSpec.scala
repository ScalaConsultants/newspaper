package io.scalac.newspaper.analyzer

import org.scalatest._

import events._

class serializationSpec extends FlatSpec with Matchers {

  "ContentFetched" should "be serialized and deserialized properly" in {
    val contentFetched = ContentFetched("foo", "bar")
    val topic = "fnord"
    val serializer = new ContentFetchedSerializer
    val deserializer = new ContentFetchedDeserializer
    deserializer.deserialize(topic, serializer.serialize(topic, contentFetched)) shouldEqual contentFetched
  }

  "ChangeDetected" should "be serialized and deserialized properly" in {
    val changeDetected = ChangeDetected("foo", "bar", "baz")
    val topic = "fnord"
    val serializer = new ChangeDetectedSerializer
    val deserializer = new ChangeDetectedDeserializer
    deserializer.deserialize(topic, serializer.serialize(topic, changeDetected)) shouldEqual changeDetected
  }

}
