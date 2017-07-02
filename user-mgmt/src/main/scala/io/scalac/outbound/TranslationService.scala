package io.scalac.outbound

import io.scalac.newspaper.events.{ChangeDetected, RequestNotification}
import scala.concurrent.Future
import scala.collection.immutable.Seq

trait TranslationService {
  def translate(event: ChangeDetected): Future[Seq[RequestNotification]]
}

class FixedTranslationService extends TranslationService {
  val emails = Seq(
    "patryk+newsfoo@scalac.io",
    "patryk+newsbar2@scalac.io"
  )

  override def translate(event: ChangeDetected): Future[Seq[RequestNotification]] = Future.successful{
    emails.map { subscriber =>
      RequestNotification(pageUrl = event.pageUrl, destinationEmail = subscriber, destinationName = "foo")
    }
  }
}
