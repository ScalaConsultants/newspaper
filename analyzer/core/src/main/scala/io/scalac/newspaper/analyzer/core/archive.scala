package io.scalac.newspaper.analyzer.core

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.{ ExecutionContext, Future }
import scala.collection.mutable
import scala.collection.JavaConverters._

trait Archive {
  def get(url: PageUrl)(implicit ec: ExecutionContext): Future[Option[PageContent]]
  def put(url: PageUrl, content: PageContent)(implicit ec: ExecutionContext): Future[Option[PageContent]]
}

class InMemoryArchive(entries: (PageUrl, PageContent)*) extends Archive {

  private val pages: mutable.Map[PageUrl, PageContent] = (new ConcurrentHashMap).asScala
  pages ++= entries

  override def get(url: PageUrl)(implicit ec: ExecutionContext): Future[Option[PageContent]] = Future.successful {
    pages.get(url)
  }

  override def put(url: PageUrl, content: PageContent)(implicit ec: ExecutionContext): Future[Option[PageContent]] = Future.successful {
    pages.put(url, content)
  }

}
