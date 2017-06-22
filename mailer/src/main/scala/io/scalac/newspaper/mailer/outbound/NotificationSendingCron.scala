package io.scalac.newspaper.mailer.outbound

import akka.actor.{Props, ActorLogging, Actor}
import io.scalac.newspaper.mailer.{MailRecipient, MailSender}
import io.scalac.newspaper.mailer.db.{SendOrders, SendOrdersRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class NotificationSendingCron(db: SendOrdersRepository, mail: MailSender) extends Actor with ActorLogging {
  import NotificationSendingCron._

  override def receive: Receive = {
    case SendNow =>
      println(s"Going to send!")
      val allNotSent = db.allReadyToSend()
      allNotSent.map {
        _.groupBy(_.forUser).map {
          case (user, orders) =>
            println(s"Trying to send for $user")
            trySendingToUser(user, orders)
        }
      }
  }

  def trySendingToUser(user: String, orders: Seq[SendOrders]): Future[Boolean] = {
    val to = MailRecipient(user)
    val content = orders.map(_.url).mkString(" \n ")
    mail.send(to, content).flatMap { _ =>
      val deliveredUpdates = orders.map(_.id).flatten
      db.markAsSent(deliveredUpdates)
    }
  }
}

object NotificationSendingCron {
  //protocol
  case object SendNow
  //utils
  def props(db: SendOrdersRepository, mail: MailSender): Props =
    Props(new NotificationSendingCron(db, mail))
}