package io.scalac.newspaper.mailer

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{Subscriptions, ConsumerSettings}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, ByteArrayDeserializer}

import scala.concurrent.ExecutionContext

object MailerRunner extends App {
  implicit val system = ActorSystem("Newspaper-Mailer-System")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val host = "192.168.99.100" //TODO: move to config
  val mailRecipient = MailRecipient("patryk+newsletter@scalac.io")
  val mailer: MailSender = buildNewMailer()

  //TODO: use Protobuff
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(s"$host:9092")
    .withGroupId("Newspaper-Mailer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val subscription = Subscriptions.topics("newspaper")
  Consumer.committableSource(consumerSettings, subscription)
    .mapAsync(1) { msg =>
      mailer.send(mailRecipient, msg.record.value()).map(_ => msg)
    }.mapAsync(1) { msg =>
      msg.committableOffset.commitScaladsl()
    }
    .runWith(Sink.ignore)



  def buildNewMailer() = {
//    new LogSender() //TODO: use some dependency injection
    new SmtpMailSender(new MailerConf(
      host = "smtp.gmail.com", //host,
      port = 587,
      user = "patryk@scalac.io", //"user1@domain.tld",
      password = "mypassword"
    ))
  }
}
