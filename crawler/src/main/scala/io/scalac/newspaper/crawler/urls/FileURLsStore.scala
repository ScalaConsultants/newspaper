package io.scalac.newspaper.crawler.urls

import java.nio.file.Path

import akka.NotUsed
import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.util.ByteString

trait FileURLsStore extends URLsStore{

  def urlsFilePath: Path

  override def getURLs: Source[String, NotUsed] =
    FileIO.fromPath(urlsFilePath)
      .via(Framing.delimiter(ByteString("\n"), 1024, true))
      .map(_.utf8String)
      .mapMaterializedValue(_ => NotUsed)
}
