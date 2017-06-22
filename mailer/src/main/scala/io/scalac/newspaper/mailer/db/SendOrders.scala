package io.scalac.newspaper.mailer.db

import java.sql.Timestamp
import java.time.LocalDateTime

import slick.jdbc.PostgresProfile.api._

object SendingOrders {

  class SendOrdersTable(tag: Tag) extends Table[SendOrders](tag, "send_orders") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def url = column[String]("url")
    def forUser = column[String]("for_user")
    def timeAdded = column[Timestamp]("time_added")
    def wasSent = column[Boolean]("was_sent")

    def * = (id.?, url, forUser, timeAdded, wasSent) <> (SendOrders.tupled, SendOrders.unapply)
  }

  val query = TableQuery[SendOrdersTable]

  //TODO: remove later, just as an example
  def addOne() = {
    val db = Database.forConfig("relational-datastore")
    try {
      db.run {
        query += SendOrders(None, "url", "user", Timestamp.valueOf(LocalDateTime.now()), false)
      }
    } finally db.close
  }
}

//TODO: user wrapper types
case class SendOrders(id: Option[Int], url: String, forUser: String, timeAdded: Timestamp, wasSent: Boolean)