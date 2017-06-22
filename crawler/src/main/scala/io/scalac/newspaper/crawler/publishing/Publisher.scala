package io.scalac.newspaper.crawler.publishing

import akka.Done
import akka.stream.scaladsl.Sink
import io.scalac.newspaper.crawler.fetching.FetchingFlow.URLFetched

import scala.concurrent.Future

trait Publisher {

  def publish: Sink[URLFetched, Future[Done]]
}
