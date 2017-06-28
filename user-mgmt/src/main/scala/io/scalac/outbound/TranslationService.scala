package io.scalac.outbound

import io.scalac.newspaper.events.{ChangeDetected, RequestNotification}

import scala.concurrent.Future


trait TranslationService {
  def translate(event: ChangeDetected): Future[Seq[RequestNotification]]
}

class FixedTranslationService extends TranslationService {
  val emails = Seq(
    "patryk+newsletter@scalac.io",
    "patryk+newsletter2@scalac.io"
  )

  override def translate(event: ChangeDetected): Future[Seq[RequestNotification]] = Future.successful{
    emails.map { subscriber =>
      RequestNotification(pageUrl = event.pageUrl, destinationEmail = subscriber, destinationName = "foo")
    }
  }
}
