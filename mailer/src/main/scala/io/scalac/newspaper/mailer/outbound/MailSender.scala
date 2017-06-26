package io.scalac.newspaper.mailer.outbound

import javax.mail.internet.InternetAddress

import scala.concurrent.Future
import scala.util.Failure

case class MailRecipient(to: String) extends AnyVal

case object MailSent

trait MailSender {
  def send(to: MailRecipient, mailContent: String): Future[MailSent.type]
}

class LogSender() extends MailSender {
  override def send(to: MailRecipient, mailContent: String) = {
    println(s"[FAKE][SENDING to: ${to.to}] $mailContent")
    Future.successful(MailSent)
  }
}

case class MailerConf(host: String, port: Int, user: String, password: String)

class SmtpMailSender(conf: MailerConf) extends MailSender {
  import courier._
  import Defaults._

  val mailer = Mailer(conf.host, conf.port)
    .auth(true)
    .as(conf.user, conf.password)
    .startTtls(true)()

  override def send(to: MailRecipient, mailContent: String): Future[MailSent.type] = {
    println(s"[SENDING to: ${to.to}] $mailContent")
    val msg = Envelope.from(new InternetAddress(conf.user))
      .to(new InternetAddress(to.to))
      .subject("Newspaper!")
      .content(Text(mailContent))

    val sendingF = mailer(msg).map(_ => MailSent)

    sendingF.onComplete {
      case Failure(ex) =>
        println(s"Message NOT delivered to ${to} due to $ex")
        println(s"--- ${ex.getStackTrace.mkString("\n  ")}")
      case _ =>
        println(s"Message was delivered to ${to}!")
    }

    sendingF
  }
}