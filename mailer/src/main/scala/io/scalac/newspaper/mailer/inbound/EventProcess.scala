package io.scalac.newspaper.mailer.inbound

import io.scalac.newspaper.events.{RequestNotification}
import io.scalac.newspaper.mailer.db.SendOrdersRepository
import io.scalac.newspaper.mailer.outbound.MailRecipient

import scala.concurrent.Future

class EventProcess(repo: SendOrdersRepository) {

  def handleEvent(event: RequestNotification): Future[Boolean] = {
    repo.add(Seq(MailRecipient(event.destinationEmail)), event.pageUrl)
  }
}
