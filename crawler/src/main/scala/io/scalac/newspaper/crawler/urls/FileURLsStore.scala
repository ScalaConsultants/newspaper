package io.scalac.newspaper.crawler.urls

import java.nio.file.Path

import akka.NotUsed
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.io

class FileURLsStore(urlsFilePath: Path)(implicit ec: ExecutionContext, materializer: ActorMaterializer) extends URLsStore {

  private var urls: immutable.Seq[String] = readData(urlsFilePath)

  private def readData(path: Path) =
    io.Source.fromFile(urlsFilePath.toFile).getLines().to[immutable.Seq]

  override def getURLs: Source[String, NotUsed] =
    Source(urls)

  def removeURL(url: String): Future[Boolean] = {
    urls = urls.filterNot(_ == url)

    Source(urls)
     .map(s => ByteString(s + "\n"))
      .runWith(FileIO.toPath(urlsFilePath))
      .map(_ => true)
  }
}
