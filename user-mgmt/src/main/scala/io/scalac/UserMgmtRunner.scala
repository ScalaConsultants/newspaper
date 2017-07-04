package io.scalac

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import io.scalac.inbound.{ChangeDetectedPBDeserializer, SlickUserRepository, SubscribeUserPBDeserializer, UserRepository}
import io.scalac.newspaper.events.RequestNotification
import io.scalac.outbound.{FixedTranslationService, RequestNotificationPBSerializer, TranslationService}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

import scala.concurrent.{ExecutionContext, Future}

object UserMgmtRunner extends App {
  implicit val system = ActorSystem("Newspaper-User-Mgmt-System")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val changeDetectedConsumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, ChangeDetectedPBDeserializer())
    .withGroupId("User-Mgmt")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new RequestNotificationPBSerializer)

  val changeDetectedSubscription = Subscriptions.topics("newspaper")

  val translateMessages: TranslationService = new FixedTranslationService()
  val userService = UserMgmtHelper.buildRepository()

  //Inbound 1: enrich Change Detected events with user data
  Consumer.committableSource(changeDetectedConsumerSettings, changeDetectedSubscription)
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

  //Inbound 2: add new users
  val subscribeUserConsumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new SubscribeUserPBDeserializer())
    .withGroupId("User-Mgmt")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val subscribeUserSubscription = Subscriptions.topics("newspaper-users")

  Consumer.committableSource(subscribeUserConsumerSettings, subscribeUserSubscription)
    .mapAsync(1) { msg =>
      userService.addOrUpdate(msg.record.value()).map(_ => msg) //TODO: check failure handling
    }.mapAsync(1) { msg =>
      msg.committableOffset.commitScaladsl()
    }
  .runWith(Sink.ignore)
}


object UserMgmtHelper {
  def buildRepository(): UserRepository = {
    import slick.jdbc.PostgresProfile.api._
    val db = Database.forConfig("relational-datastore")
    new SlickUserRepository(db)
  }
}
