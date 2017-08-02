package io.scalac.newspaper.analyzer.kafka

import scala.concurrent.Future
import scala.language.implicitConversions

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.{TestPublisher, TestSubscriber}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestKit
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.scalamock.proxy.Mock
import org.scalamock.scalatest.proxy.MockFactory
import org.scalatest._

import io.scalac.newspaper.analyzer.core._
import io.scalac.newspaper.analyzer.kafka.AnalyzerFlow.{ChangeDetectedMessage, ContentFetchedCommittableMessage, Key}
import io.scalac.newspaper.events._

class AnalyzerFlowSpec extends TestKit(ActorSystem("test-system")) with WordSpecLike with Matchers with MockFactory {

  import system.dispatcher

  implicit val materializer = ActorMaterializer()

  def testFlow[T](body: (Analyzer with Mock, Archive with Mock, CommittableOffset with Mock, TestPublisher.Probe[ContentFetchedCommittableMessage], TestSubscriber.Probe[ChangeDetectedMessage]) => T): T = {
    val maxParallelism = 4
    val analyzer = mock[Analyzer]
    val archive = mock[Archive]
    val offset = mock[CommittableOffset]
    val logger = Logger("analyzer")
    val analyzerFlow = AnalyzerFlow(analyzer, archive, logger, maxParallelism)

    val (pub, sub) = TestSource.probe[ContentFetchedCommittableMessage]
      .via(analyzerFlow)
      .toMat(TestSink.probe[ChangeDetectedMessage])(Keep.both)
      .run

    body(analyzer, archive, offset, pub, sub)
  }

  def contentFetchedMessage(contentFetched: ContentFetched, offset: CommittableOffset): ContentFetchedCommittableMessage = {
    val record = new ConsumerRecord[Key, ContentFetched]("newspaper-content", 0, 0, null, contentFetched)
    CommittableMessage(record, offset)
  }

  "AnalyzerFlow" should {
    "commit offset without producing messages in case of no changes" in {
      testFlow { (analyzer, archive, offset, pub, sub) =>
        inSequence {
          archive.expects('put)(PageUrl("foo"), PageContent("bar"), *).returns(Future(Some(PageContent("bar"))))
          analyzer.expects('checkForChanges)(Some(PageContent("bar")), PageContent("bar")).returns(List.empty)
          offset.expects('commitScaladsl)()
        }

        sub.request(1)
        pub.sendNext(contentFetchedMessage(ContentFetched("foo", "bar"), offset))
        pub.sendComplete()
        sub.expectComplete()
      }
    }

    "commit offset after producing one message in case of one change" in {
      testFlow { (analyzer, archive, offset, pub, sub) =>
        inSequence {
          archive.expects('put)(PageUrl("foo"), PageContent("baz"), *).returns(Future(Some(PageContent("bar"))))
          analyzer.expects('checkForChanges)(Some(PageContent("bar")), PageContent("baz")).returns(List(Change("baz")))
        }

        sub.request(1)
        pub.sendNext(contentFetchedMessage(ContentFetched("foo", "baz"), offset))
        val result = sub.expectNext()
        result.record.value shouldEqual ChangeDetected("foo", "baz")
        result.passThrough shouldEqual Some(offset)
        pub.sendComplete()
        sub.expectComplete()
      }
    }

    "commit offset after producing two messages in case of two changes" in {
      testFlow { (analyzer, archive, offset, pub, sub) =>
        inSequence {
          archive.expects('put)(PageUrl("foo"), PageContent("baz fnord"), *).returns(Future(Some(PageContent("bar"))))
          analyzer.expects('checkForChanges)(Some(PageContent("bar")), PageContent("baz fnord")).returns(List(Change("baz"), Change("fnord")))
        }

        sub.request(2)
        pub.sendNext(contentFetchedMessage(ContentFetched("foo", "baz fnord"), offset))
        val result1 = sub.expectNext()
        result1.record.value shouldEqual ChangeDetected("foo", "baz")
        result1.passThrough shouldEqual None
        val result2 = sub.expectNext()
        result2.record.value shouldEqual ChangeDetected("foo", "fnord")
        result2.passThrough shouldEqual Some(offset)
        pub.sendComplete()
        sub.expectComplete()
      }
    }

  }

}
