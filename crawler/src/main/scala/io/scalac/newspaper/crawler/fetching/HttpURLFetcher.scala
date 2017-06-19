package io.scalac.newspaper.crawler.fetching

import io.scalac.newspaper.crawler.fetching.FetchingFlow._
import io.scalac.newspaper.crawler.fetching.HttpURLFetcher._
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, TimeoutException}

trait HttpURLFetcher {

  implicit val ec: ExecutionContext

  def urlFetcherConfig: URLFetcherConfig
  def wsClient: StandaloneWSClient

  protected def get(url: String): Future[URLFetchingResult] =
    wsClient
      .url(url)
      .withRequestTimeout(urlFetcherConfig.requestTimeout)
      .get()
      .map(response => if (responseFetchedCorrectly(response)) URLFetched(url, response.body)
        else URLNotFetched(url, response.status, response.body)
      )

  def fetchURL(url: String): Future[URLFetchingResult] =
      get(url)
      .recover {
        case _: TimeoutException => URLFetchingTimeout(url)
        case ex: Throwable => URLFetchingException(url, ex)
      }

  private def responseFetchedCorrectly(response: StandaloneWSResponse): Boolean =
    response.status == 200
}

object HttpURLFetcher {
  case class URLFetcherConfig(requestTimeout: Duration)
}