package io.scalac.newspaper.crawler.failure

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.stream.scaladsl.Source
import akka.testkit.{ImplicitSender, TestKit}
import io.scalac.newspaper.crawler.RestartableActor.RestartActor
import io.scalac.newspaper.crawler.{InMemoryCleanup, RestartableActor}
import io.scalac.newspaper.crawler.failure.FailureHandler.{FailureHandlerConfig, GetFailures, URLFailures}
import io.scalac.newspaper.crawler.fetching.FetchingFlow.{URLFetchingException, URLFetchingTimeout, URLNotFetched}
import io.scalac.newspaper.crawler.urls.URLsStore
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.mutable
import scala.concurrent.Future


class FailureHandlerSpec extends TestKit(ActorSystem("test-system")) with ImplicitSender with WordSpecLike with BeforeAndAfterAll with InMemoryCleanup with Matchers {

  override def afterAll(): Unit =
    TestKit.shutdownActorSystem(system)

  private val config = FailureHandlerConfig(3, 100)
  private val url = "URL"

  "FailureHandler" should {
    "store all url fetching failures grouped by URL" in {
      val url2 = "URL2"
      val cut = system.actorOf(FailureHandler.props(new TestURLsStore, config))

      cut ! URLNotFetched(url, 404, "Not Found")
      cut ! URLFetchingTimeout(url2)
      cut ! URLNotFetched(url, 404, "Not Found")

      cut ! GetFailures(url)
      expectMsgPF() {
        case URLFailures(Some(failures)) => failures.size shouldEqual 2
      }

      cut ! GetFailures(url2)
      expectMsgPF() {
        case URLFailures(Some(failures)) => failures.size shouldEqual 1
      }

      cut ! GetFailures("successfulURL")
      expectMsgPF() {
        case URLFailures(failures) => failures shouldBe None
      }
    }

    "should remove url after reaching failures limit" in {
      val urlsStore = new TestURLsStore
      val cut = system.actorOf(FailureHandler.props(urlsStore, config))

      cut ! URLFetchingTimeout(url)
      cut ! URLNotFetched(url, 403, "You're not authorized")
      cut ! URLFetchingTimeout(url)

      cut ! GetFailures(url)
      expectMsgType[URLFailures]

      urlsStore.removeURLCallCounter.get(url) shouldEqual Some(1)
    }

    "should recover it's state after restart" in {
      val cut = system.actorOf(Props(new FailureHandler(new TestURLsStore, config) with RestartableActor))
      val url = "URL"

      cut ! URLFetchingException(url, new IllegalArgumentException())
      cut ! URLNotFetched(url, 403, "You're not authorized")

      cut ! RestartActor

      cut ! GetFailures(url)
      expectMsgPF() {
        case URLFailures(Some(failures)) => failures.size shouldEqual 2
      }
    }
  }

  class TestURLsStore extends URLsStore {
    val removeURLCallCounter = mutable.Map.empty[String, Int]

    override def getURLs: Source[String, NotUsed] = Source.empty[String]

    override def removeURL(url: String): Future[Boolean] = {
      val calls = removeURLCallCounter.getOrElse(url, 0)
      removeURLCallCounter.put(url, calls +1)
      Future.successful(true)
    }
  }

}
