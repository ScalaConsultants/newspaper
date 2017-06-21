package io.scalac.newspaper.crawler.urls

import akka.NotUsed
import akka.stream.scaladsl.Source

trait URLsStore {

  def getURLs: Source[String, NotUsed]
}
