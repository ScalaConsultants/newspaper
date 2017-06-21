package io.scalac.newspaper.crawler.fetching

import akka.NotUsed
import akka.stream.scaladsl.Flow
import io.scalac.newspaper.crawler.fetching.FetchingFlow._
import io.scalac.newspaper.crawler.fetching.HttpFetchingFlow.FetchingConfig
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, TimeoutException}

trait HttpFetchingFlow extends FetchingFlow {

  def fetchingConfig: FetchingConfig

  def fetchURLs: Flow[String, URLFetchingResult, NotUsed] =
    Flow[String].mapAsyncUnordered(fetchingConfig.maxParallelism)(fetchURL)

  implicit def ec: ExecutionContext

  def wsClient: StandaloneWSClient

  protected def get(url: String): Future[URLFetchingResult] =
    wsClient
      .url(url)
      .withRequestTimeout(fetchingConfig.requestTimeout)
      .get()
      .map(response => if (responseFetchedCorrectly(response)) URLFetched(url, response.body)
      else URLNotFetched(url, response.status, response.body)
      )

  private def fetchURL(url: String): Future[URLFetchingResult] =
    get(url)
      .recover {
        case _: TimeoutException => URLFetchingTimeout(url)
        case ex: Throwable => URLFetchingException(url, ex)
      }

  private def responseFetchedCorrectly(response: StandaloneWSResponse): Boolean =
    response.status == 200
}

object HttpFetchingFlow {
  case class FetchingConfig(maxParallelism: Int, requestTimeout: Duration)
}