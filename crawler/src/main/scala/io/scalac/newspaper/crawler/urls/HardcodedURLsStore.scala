package io.scalac.newspaper.crawler.urls

import akka.NotUsed
import akka.stream.scaladsl.Source
import scala.collection.immutable.Seq

trait HardcodedURLsStore extends URLsStore {
  override def getURLs: Source[String, NotUsed] = Source(
    Seq(
      "https://blog.scalac.io/"
    )
  )
}
