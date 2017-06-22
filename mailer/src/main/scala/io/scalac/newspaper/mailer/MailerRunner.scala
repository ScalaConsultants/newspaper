package io.scalac.newspaper.mailer

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{Subscriptions, ConsumerSettings}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import io.scalac.newspaper.mailer.db.{SlickSendOrdersRepository, SendOrdersRepository}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, ByteArrayDeserializer}

import scala.concurrent.ExecutionContext

object MailerRunner extends App {
  implicit val system = ActorSystem("Newspaper-Mailer-System")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val configuration = ConfigFactory.load()

  val mailRecipient = MailRecipient("patryk+newsletter@scalac.io")
  val mailer: MailSender = buildNewMailerSender()

  val process = buildNewMailingProcess()

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, ChangeDetectedPBDeserializer())
    .withGroupId("Newspaper-Mailer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val subscription = Subscriptions.topics("newspaper")
  Consumer.committableSource(consumerSettings, subscription)
    .mapAsync(1) { msg =>
//      mailer.send(mailRecipient, msg.record.value().pageUrl).map(_ => msg)
      process.handleEvent(msg.record.value()).map(_ => msg)
    }.mapAsync(1) { msg =>
      msg.committableOffset.commitScaladsl()
    }
    .runWith(Sink.ignore)

  def buildNewMailerSender() = {
    val mailingConf = configuration.getConfig("email")
    mailingConf.getBoolean("debug") match {
      case false =>
        new SmtpMailSender(new MailerConf(
          host = mailingConf.getString("host"),
          port = mailingConf.getInt("port"),
          user = mailingConf.getString("user"),
          password = mailingConf.getString("password")
        ))
      case _ =>
        new LogSender()
    }
  }

  def buildNewMailingProcess() = {
    import slick.jdbc.PostgresProfile.api._
    val db = Database.forConfig("relational-datastore")
    new MailingProcess(new SlickSendOrdersRepository(db))
  }
}
