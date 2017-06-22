package io.scalac.newspaper.analyzer

import akka.actor.ActorSystem
import akka.kafka.{ ConsumerMessage, ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions }
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Sink}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, ByteArraySerializer }

import io.scalac.newspaper.events._

object AnalyzerRunner extends App {

  implicit val system = ActorSystem("Newspaper-Analyzer-System")
  implicit val materializer = ActorMaterializer()

  val analyzer = new Analyzer

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new ContentFetchedDeserializer)
    .withGroupId("Newspaper-Analyzer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new ChangeDetectedSerializer)

  val subscription = Subscriptions.topics("newspaper-content")
  Consumer.committableSource(consumerSettings, subscription)
    .mapConcat { msg =>
      println(s"[ANALYZING] ${msg.record.value.pageUrl}")

      val input = msg.record.value
      val url = input.pageUrl
      val changes = analyzer.checkForChanges(url, input.pageContent)

      // We want to commit the offset only after producing the last message
      // in order to ensure at-least-once delivery.
      // TODO: Optimize?
      def recurse(changesRemaining: List[Analyzer.Change]): List[ProducerMessage.Message[Array[Byte], ChangeDetected, Option[ConsumerMessage.CommittableOffset]]] = changesRemaining match {
        case Nil => Nil

        case change :: Nil =>
          val record = new ProducerRecord[Array[Byte], ChangeDetected]("newspaper", ChangeDetected(url, change.content))
          println(s"[CHANGE+COMMIT] ${msg.record.value.pageUrl}")
          ProducerMessage.Message(record, Some(msg.committableOffset)) :: Nil

        case change :: rest =>
          val record = new ProducerRecord[Array[Byte], ChangeDetected]("newspaper", ChangeDetected(url, change.content))
          println(s"[CHANGE] ${msg.record.value.pageUrl}")
          ProducerMessage.Message(record, None) :: recurse(rest)
      }

      val messages = recurse(changes)
      if (messages.isEmpty) {
        // No changes detected, we need to commit the offset manually
        msg.committableOffset.commitScaladsl()
      }
      messages
    }
    .via(Producer.flow(producerSettings))
    .map(_.message.passThrough)
    .collect{ case Some(offset) => offset }
    .mapAsync(1)(_.commitScaladsl())
    .runWith(Sink.ignore)

}
