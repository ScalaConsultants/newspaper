package io.scalac

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import io.scalac.inbound.ChangeDetectedPBDeserializer
import io.scalac.outbound.{FixedTranslationService, TranslationService}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer

import scala.concurrent.ExecutionContext

object UserMgmtRunner extends App {
  implicit val system = ActorSystem("Newspaper-User-Mgmt-System")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, ChangeDetectedPBDeserializer())
    .withGroupId("User-Mgmt")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val subscription = Subscriptions.topics("newspaper")

  val translateMessages: TranslationService = new FixedTranslationService()

  Consumer.committableSource(consumerSettings, subscription)
    .mapAsync(1) { msg =>
      translateMessages.translate(msg.record.value()).map(_ => msg)// TODO: publish the command!
    }.mapAsync(1) { msg =>
      msg.committableOffset.commitScaladsl()
    }.runWith(Sink.ignore)
}
