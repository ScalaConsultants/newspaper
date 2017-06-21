package io.scalac.newspaper.crawler.fetching

import akka.NotUsed
import akka.stream.scaladsl.Flow
import io.scalac.newspaper.crawler.fetching.FetchingFlow.URLFetchingResult

trait FetchingFlow {

  def fetchURLs: Flow[String, URLFetchingResult, NotUsed]
}

object FetchingFlow {
  trait URLFetchingResult
  case class URLFetched(url: String, content: String) extends URLFetchingResult
  case class URLNotFetched(url: String, responseCode: Int, content: String) extends URLFetchingResult
  case class URLFetchingTimeout(url: String) extends URLFetchingResult
  case class URLFetchingException(url: String, cause: Throwable) extends URLFetchingResult
}