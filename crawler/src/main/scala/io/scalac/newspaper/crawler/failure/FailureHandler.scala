package io.scalac.newspaper.crawler.failure

import akka.Done
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.StrictLogging
import io.scalac.newspaper.crawler.fetching.FetchingFlow.{URLFetchingException, URLFetchingResult, URLFetchingTimeout, URLNotFetched}

import scala.concurrent.Future

trait FailureHandler extends StrictLogging {

  def handleFailure: Sink[URLFetchingResult, Future[Done]] = Sink.foreach {
    case URLNotFetched(url, responseCode, content) => logger.error(s"Unable to fetch: $url, response code: $responseCode, response body: $content")
    case URLFetchingTimeout(url) => logger.error(s"Timeout when trying to fetch: $url")
    case URLFetchingException(url, ex) => logger.error(s"Exception when trying to fetch url: $url", ex)
  }
}
