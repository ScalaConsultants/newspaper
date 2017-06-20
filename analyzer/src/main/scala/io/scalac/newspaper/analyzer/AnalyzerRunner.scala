package io.scalac.newspaper.analyzer

import akka.actor.ActorSystem
import akka.kafka.ProducerMessage
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{Subscriptions, ConsumerSettings, ProducerSettings}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Sink}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{ProducerRecord}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

import io.scalac.newspaper.events._

object AnalyzerRunner extends App {
  implicit val system = ActorSystem("Newspaper-Analyzer-System")
  implicit val materializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new ContentFetchedDeserializer)
    .withGroupId("Newspaper-Analyzer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new ChangeDetectedSerializer)

  val subscription = Subscriptions.topics("newspaper-content")
  Consumer.committableSource(consumerSettings, subscription)
    .map { msg =>
      // Do sth with msg.record.value
      println(s"[ANALYZING] ${msg.record.value}")
      val input = msg.record.value
      val output = ChangeDetected(input.pageUrl, input.pageContent)
      val record = new ProducerRecord[Array[Byte], ChangeDetected]("newspaper", output)
      ProducerMessage.Message(record, msg.committableOffset)
    }
    .via(Producer.flow(producerSettings))
    .map(_.message.passThrough)
    .mapAsync(1)(_.commitScaladsl())
    .runWith(Sink.ignore)

}
