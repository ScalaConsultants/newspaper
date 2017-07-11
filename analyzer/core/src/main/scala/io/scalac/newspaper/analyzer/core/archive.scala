package io.scalac.newspaper.analyzer.core

import scala.concurrent.Future
import scala.collection.mutable

trait Archive {
  def get(url: PageUrl): Future[Option[PageContent]]
  def put(url: PageUrl, content: PageContent): Future[Option[PageContent]]
}

class InMemoryArchive(entries: (PageUrl, PageContent)*) extends Archive {

  private val urls: mutable.Map[PageUrl, PageContent] = mutable.HashMap(entries: _*)

  override def get(url: PageUrl): Future[Option[PageContent]] = Future.successful {
    urls.get(url)
  }

  override def put(url: PageUrl, content: PageContent): Future[Option[PageContent]] = Future.successful {
    urls.put(url, content)
  }

}
