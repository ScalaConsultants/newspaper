package io.scalac.newspaper.crawler.urls

import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.Future

trait URLsStore {

  def getURLs: Source[String, NotUsed]

  def removeURL(url: String): Future[Boolean]
}
