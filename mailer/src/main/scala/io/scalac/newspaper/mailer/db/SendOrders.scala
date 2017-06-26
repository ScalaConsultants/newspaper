package io.scalac.newspaper.mailer.db

import java.sql.Timestamp
import java.time.LocalDateTime
import io.scalac.newspaper.mailer.outbound.MailRecipient
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait SendOrdersRepository {
  //TODO: use wrapper types
  def add(users: Seq[MailRecipient], url: String): Future[Boolean]
  def allReadyToSend(): Future[Seq[SendOrders]]
  def markAsSent(ids: Seq[Int]): Future[Boolean]
}

class SlickSendOrdersRepository(db: Database) extends SendOrdersRepository {
  import SlickSendOrdersRepository._

  override def add(users: Seq[MailRecipient], url: String): Future[Boolean] = {
    val orders = users.map { email =>
      SendOrders(None, url, email, Timestamp.valueOf(LocalDateTime.now()), false)
    }
    db.run {
      query ++= orders
    }.map(_ => true)
  }

  override def allReadyToSend(): Future[Seq[SendOrders]] = {
    db.run {
      query.filter(_.wasSent === false).result
    }
  }

  override def markAsSent(ids: Seq[Int]): Future[Boolean] = {
    db.run {
      query.filter(_.wasSent === false).map(_.wasSent).update(true)
    }.map(_ => true)
  }
}

object SlickSendOrdersRepository {
  implicit val entityTypeMapper = MappedColumnType.base[MailRecipient, String] (
    r => r.to,
    MailRecipient.apply)

  private[this] class SendOrdersTable(tag: Tag) extends Table[SendOrders](tag, "send_orders") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def url = column[String]("url")
    def forUser = column[MailRecipient]("for_user")
    def timeAdded = column[Timestamp]("time_added")
    def wasSent = column[Boolean]("was_sent")

    def * = (id.?, url, forUser, timeAdded, wasSent) <> (SendOrders.tupled, SendOrders.unapply)
  }

  private val query = TableQuery[SendOrdersTable]
}


case class SendOrders(id: Option[Int], url: String, forUser: MailRecipient, timeAdded: Timestamp, wasSent: Boolean)