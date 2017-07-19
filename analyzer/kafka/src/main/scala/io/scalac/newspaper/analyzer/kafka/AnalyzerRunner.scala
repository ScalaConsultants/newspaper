package io.scalac.newspaper.analyzer.kafka

import akka.actor.ActorSystem
import akka.kafka.{ ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions }
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink }
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, ByteArraySerializer }
import scala.concurrent.ExecutionContext.Implicits.global

import io.scalac.newspaper.events._
import io.scalac.newspaper.analyzer.core._
import io.scalac.newspaper.analyzer.db.postgres._

object AnalyzerRunner extends App {

  def mapLastDifferently[A, B](list: List[A])(f: A => B)(fLast: A => B): List[B] = {
    @annotation.tailrec
    def loop(mapped: List[B], rest: List[A]): List[B] = rest match {
      case Nil => mapped
      case x :: Nil => fLast(x) :: mapped
      case x :: ys => loop(f(x) :: mapped, ys)
    }

    loop(Nil, list).reverse
  }

  implicit val system = ActorSystem("Newspaper-Analyzer-System")
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val maxParallelism = config.getInt("maxParallelism")

  val logger = Logger("analyzer")

  val archive  = new PostgresArchive
  val analyzer = new Analyzer

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new ContentFetchedDeserializer)
    .withGroupId("Newspaper-Analyzer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new ChangeDetectedSerializer)

  val subscription = Subscriptions.topics("newspaper-content")
  val done = Consumer.committableSource(consumerSettings, subscription)
    .mapAsync(maxParallelism) { msg =>
      logger.debug(s"[ANALYZING] ${msg.record.value.pageUrl}")

      val input = msg.record.value
      val url = PageUrl(input.pageUrl)
      val newContent = PageContent(input.pageContent)

      for {
        oldContent <- archive.put(url, newContent)
      } yield {
        val changes = analyzer.checkForChanges(oldContent, newContent)

        val records = changes.map { change =>
          val event = ChangeDetected(url.url, change.content)
          new ProducerRecord[Array[Byte], ChangeDetected]("newspaper", event)
        }

        // We want to commit the offset only after producing the last message
        // in order to ensure at-least-once delivery.
        val messages = mapLastDifferently(records) { record =>
          logger.debug(s"[CHANGE] ${url}")
          ProducerMessage.Message(record, None: Option[CommittableOffset])
        } { record =>
          logger.debug(s"[CHANGE+COMMIT] ${url}")
          ProducerMessage.Message(record, Some(msg.committableOffset))
        }

        if (messages.isEmpty) {
          // No changes detected, we need to commit the offset manually
          msg.committableOffset.commitScaladsl()
        }

        messages
      }
    }
    .mapConcat(identity)
    .via(Producer.flow(producerSettings))
    .map(_.message.passThrough)
    .collect{ case Some(offset) => offset }
    .mapAsync(maxParallelism)(_.commitScaladsl())
    .runWith(Sink.ignore)

  done.onComplete { _ =>
    logger.info("Shutting down...")
    system.terminate()
    archive.close()
  }

}
