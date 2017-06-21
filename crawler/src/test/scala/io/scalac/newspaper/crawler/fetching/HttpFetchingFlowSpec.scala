package io.scalac.newspaper.crawler.fetching

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import io.scalac.newspaper.crawler.fetching.FetchingFlow._
import io.scalac.newspaper.crawler.fetching.HttpFetchingFlow.FetchingConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.ws.StandaloneWSClient
import scala.concurrent.duration._

import scala.collection.immutable.{Seq, Set}
import scala.concurrent.{ExecutionContext, Future, TimeoutException}

class HttpFetchingFlowSpec extends TestKit(ActorSystem("test-system")) with WordSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll {

  implicit val materializer = ActorMaterializer()
  val illegalArgumentException = new IllegalArgumentException()

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  val cut = new HttpFetchingFlow {
    override def fetchingConfig: FetchingConfig = FetchingConfig(3, 10 seconds)

    override def get(url: String): Future[URLFetchingResult] = url match {
      case "timeout" => Future.failed(new TimeoutException())
      case "exception" => Future.failed(illegalArgumentException)
      case "badRequest" => Future.successful(URLNotFetched(url, 404, "BadRequest"))
      case url: String => Future.successful(url2URLFetched(url))
    }

    override implicit val ec: ExecutionContext = system.dispatcher

    override def wsClient: StandaloneWSClient = ???
  }

  "HttpFetchingFlow" should {
    "fetch provided urls" in {
      val urls = Seq("url1", "url2", "url3")

      val result = Source(urls)
        .via(cut.fetchURLs)
        .runWith(Sink.seq)
        .futureValue

      result.toSet shouldEqual urls.map(url2URLFetched).toSet
    }

    "handle timeouts and exceptions" in {

      val urls = Seq("url1", "timeout", "exception", "url2", "badRequest", "url3")

      val result = Source(urls)
          .via(cut.fetchURLs)
          .runWith(Sink.seq)
          .futureValue

      result.toSet shouldEqual Set(
        url2URLFetched("url1"),
        url2URLFetched("url2"),
        url2URLFetched("url3"),
        URLNotFetched("badRequest", 404, "BadRequest"),
        URLFetchingException("exception", illegalArgumentException),
        URLFetchingTimeout("timeout")
      )
    }
  }

  private def url2URLFetched(url: String): URLFetched = URLFetched(url, "CONTENT")
}
