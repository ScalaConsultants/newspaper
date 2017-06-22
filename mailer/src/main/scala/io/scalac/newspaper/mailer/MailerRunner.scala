package io.scalac.newspaper.mailer

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{Subscriptions, ConsumerSettings}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.{Config, ConfigFactory}
import io.scalac.newspaper.mailer.db.{SlickSendOrdersRepository, SendOrdersRepository}
import io.scalac.newspaper.mailer.inbound.{EventProcess, ChangeDetectedPBDeserializer}
import io.scalac.newspaper.mailer.outbound._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object MailerRunner extends App {
  import MailerRunnerHelper._
  implicit val system = ActorSystem("Newspaper-Mailer-System")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val configuration = ConfigFactory.load()

  val mailer: MailSender = buildNewMailerSender(configuration)
  val repo: SendOrdersRepository = buildRepository()
  val process = buildNewMailingProcess(repo)

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, ChangeDetectedPBDeserializer())
    .withGroupId("Newspaper-Mailer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val subscription = Subscriptions.topics("newspaper")

  Consumer.committableSource(consumerSettings, subscription)
    .mapAsync(1) { msg =>
      process.handleEvent(msg.record.value()).map(_ => msg)
    }.mapAsync(1) { msg =>
      msg.committableOffset.commitScaladsl()
    }
    .runWith(Sink.ignore)

  startCronActor(configuration, system, repo, mailer)
}

object MailerRunnerHelper {
  def buildNewMailerSender(configuration: Config) = {
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

  def buildRepository() = {
    import slick.jdbc.PostgresProfile.api._
    val db = Database.forConfig("relational-datastore")
    new SlickSendOrdersRepository(db)
  }

  def buildNewMailingProcess(repo: SendOrdersRepository) = {
    new EventProcess(repo)
  }

  def startCronActor(configuration: Config,
                     system: ActorSystem,
                     repository: SendOrdersRepository,
                     mailer: MailSender) = {
    implicit val ec =  system.dispatcher
    val cron = system.actorOf(NotificationSendingCron.props(repository, mailer))

    val delay = configuration.getInt("mailing-cron.delay")
    val interval = configuration.getInt("mailing-cron.interval")

    system.scheduler.schedule(
      Duration(delay, TimeUnit.SECONDS),
      Duration(interval, TimeUnit.SECONDS),
      cron,
      NotificationSendingCron.SendNow
    )
  }
}
