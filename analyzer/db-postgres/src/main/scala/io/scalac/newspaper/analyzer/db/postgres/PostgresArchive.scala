package io.scalac.newspaper.analyzer.db.postgres

import scala.concurrent.{ ExecutionContext, Future }
import slick.jdbc.PostgresProfile.api._

import io.scalac.newspaper.analyzer.core._

class PostgresArchive extends Archive {

  import PostgresArchive._

  private val db = Database.forConfig("db-postgres")

  override def get(url: PageUrl)(implicit ec: ExecutionContext): Future[Option[PageContent]] = {
    db.run {
      getQuery(url.url).result.headOption
    }.map(_.map(page => PageContent(page.content)))
  }

  override def put(url: PageUrl, content: PageContent)(implicit ec: ExecutionContext): Future[Option[PageContent]] = {
    db.run {
      (pages returning pages).insertOrUpdate(Page(url.url, content.content))
    }.map(_.map(page => PageContent(page.content)))
  }

  def close() = db.close

}

object PostgresArchive {

  private final case class Page(url: String, content: String)

  private class PageTable(tag: Tag) extends Table[Page](tag, "pages") {
    def url     = column[String]("url", O.PrimaryKey)
    def content = column[String]("content")

    def * = (url, content) <> (Page.tupled, Page.unapply)
  }

  private val pages = TableQuery[PageTable]

  private val getQuery = Compiled { (url: Rep[String]) =>
    pages.filter(_.url === url)
  }

}
