package io.scalac.newspaper.crawler.fetching

import akka.NotUsed
import akka.stream.scaladsl.Flow
import io.scalac.newspaper.crawler.fetching.FetchingFlow.URLFetchingResult
import io.scalac.newspaper.crawler.fetching.HttpFetchingFlow.FetchingConfig

trait HttpFetchingFlow extends FetchingFlow {
  this: HttpURLFetcher =>

  def fetchingConfig: FetchingConfig

  def fetchURLs: Flow[String, URLFetchingResult, NotUsed] =
    Flow[String].mapAsyncUnordered(fetchingConfig.maxParallelism)(fetchURL)
}

object HttpFetchingFlow {
  case class FetchingConfig(maxParallelism: Int)
}