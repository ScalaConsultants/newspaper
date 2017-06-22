package io.scalac.newspaper.mailer.inbound

import io.scalac.newspaper.events.ChangeDetected
import io.scalac.newspaper.mailer.db.SendOrdersRepository
import io.scalac.newspaper.mailer.outbound.MailRecipient

import scala.concurrent.Future

class MailingProcess(repo: SendOrdersRepository) {
  val emails = Seq(
    MailRecipient("patryk+newsletter@scalac.io"),
    MailRecipient("patryk+newsletter2@scalac.io")
  )

  def handleEvent(event: ChangeDetected): Future[Boolean] = {
    repo.add(emails, event.pageUrl)
  }
}
