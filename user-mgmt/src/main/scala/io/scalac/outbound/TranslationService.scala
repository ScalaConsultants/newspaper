package io.scalac.outbound

import io.scalac.inbound.UserRepository
import io.scalac.newspaper.events.{ChangeDetected, RequestNotification}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.Seq

trait TranslationService {
  def translate(event: ChangeDetected): Future[Seq[RequestNotification]]
}

class SlickTranslationService(users: UserRepository) extends TranslationService {

  override def translate(event: ChangeDetected): Future[Seq[RequestNotification]] = {
    users.getAll().map { users =>
      Seq(users: _*).map { subscriber =>
        RequestNotification(
          pageUrl = event.pageUrl,
          destinationEmail = subscriber.userEmail.to,
          destinationName = subscriber.userName)
      }
    }
  }
}
