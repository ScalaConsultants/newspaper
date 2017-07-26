package io.scalac.newspaper.analyzer.kafka

import scala.concurrent.ExecutionContext

import akka.NotUsed
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import akka.kafka.ProducerMessage
import akka.kafka.ProducerMessage.Message
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.Logger
import io.scalac.newspaper.analyzer.core._
import io.scalac.newspaper.events._
import org.apache.kafka.clients.producer.ProducerRecord

object AnalyzerFlow {

  type ContentFetchedCommittableMessage = CommittableMessage[Array[Byte], ContentFetched]
  type ChangeDetectedMessage = Message[Array[Byte], ChangeDetected, Option[CommittableOffset]]

  def apply(
    analyzer       : Analyzer,
    archive        : Archive,
    logger         : Logger,
    maxParallelism : Int
  )(implicit ec: ExecutionContext): Flow[ContentFetchedCommittableMessage, ChangeDetectedMessage, NotUsed] = Flow[ContentFetchedCommittableMessage]
    .mapAsync(maxParallelism) { msg =>
      logger.debug(s"[ANALYZING] ${msg.record.value.pageUrl}")

      val input = msg.record.value
      val url = PageUrl(input.pageUrl)
      val newContent = PageContent(input.pageContent)

      for {
        // FIXME: Doing a write here breaks at-least-once semantics
        //        https://github.com/ScalaConsultants/newspaper/issues/37
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

  private[this] def mapLastDifferently[A, B](list: List[A])(f: A => B)(fLast: A => B): List[B] = {
    @annotation.tailrec
    def loop(mapped: List[B], rest: List[A]): List[B] = rest match {
      case Nil => mapped
      case x :: Nil => fLast(x) :: mapped
      case x :: ys => loop(f(x) :: mapped, ys)
    }

    loop(Nil, list).reverse
  }

}
