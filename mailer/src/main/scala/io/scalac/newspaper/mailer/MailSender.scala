package io.scalac.newspaper.mailer

import scala.concurrent.Future

case class MailRecipient(to: String) extends AnyVal

case object MailSent

trait MailSender {
  def send(to: MailRecipient, mailContent: String): Future[MailSent.type]
}

class LogSender() extends MailSender{
  override def send(to: MailRecipient, mailContent: String) = {
    println(s"[SENDING to: ${to.to}] $mailContent")
    Future.successful(MailSent)
  }
}