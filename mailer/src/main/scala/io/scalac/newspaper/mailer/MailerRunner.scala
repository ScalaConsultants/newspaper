package io.scalac.newspaper.mailer

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{Subscriptions, ConsumerSettings}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, ByteArrayDeserializer}

object MailerRunner extends App {
  implicit val system = ActorSystem("Newspaper-Mailer-System")
  implicit val materializer = ActorMaterializer()

  val host = "192.168.99.100" //TODO: move to config
  val mailRecipient = MailRecipient("patryk@scalac.io")
  val mailer: MailSender = new LogSender() //TODO: use some dependency injection

  //TODO: use Protobuff
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(s"$host:9092")
    .withGroupId("Newspaper-Mailer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val subscription = Subscriptions.topics("newspaper")
  Consumer.committableSource(consumerSettings, subscription)
    .mapAsync(1) { msg =>
      mailer.send(mailRecipient, msg.record.value())
      msg.committableOffset.commitScaladsl()
    }
    .runWith(Sink.ignore)
}
