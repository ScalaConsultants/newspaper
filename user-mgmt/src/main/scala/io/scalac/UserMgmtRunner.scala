package io.scalac

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import io.scalac.inbound.ChangeDetectedPBDeserializer
import io.scalac.newspaper.events.{RequestNotification}
import io.scalac.outbound.{FixedTranslationService, RequestNotificationPBSerializer, TranslationService}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

import scala.concurrent.{ExecutionContext}

object UserMgmtRunner extends App {
  implicit val system = ActorSystem("Newspaper-User-Mgmt-System")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, ChangeDetectedPBDeserializer())
    .withGroupId("User-Mgmt")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new RequestNotificationPBSerializer)

  val inboundSubscription = Subscriptions.topics("newspaper")

  val translateMessages: TranslationService = new FixedTranslationService()

  Consumer.committableSource(consumerSettings, inboundSubscription)
    .mapAsyncUnordered(10) { msg => // we don't care about order
      translateMessages.translate(msg.record.value()).map { case publishRequests =>
        publishRequests.map { case publishRequest =>
          val record = new ProducerRecord[Array[Byte], RequestNotification]("newspaper-notifications", publishRequest)
          ProducerMessage.Message(record, msg.committableOffset)
        }
      }
    }
    .mapConcat(identity)
    .via(Producer.flow(producerSettings))
    .map(_.message.passThrough)
    .mapAsync(1) { msg =>
      msg.commitScaladsl()
    }.runWith(Sink.ignore)
}
