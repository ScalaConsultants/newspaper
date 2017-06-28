package io.scalac.newspaper.mailer.outbound

import java.sql.Timestamp

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import io.scalac.newspaper.mailer.db.{SendOrders, SendOrdersRepository}
import io.scalac.newspaper.mailer.outbound.NotificationSendingCron.SendNow
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import org.scalatest.{MustMatchers, BeforeAndAfterAll, WordSpecLike}
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class NotificationSendingCronSpec() extends TestKit(ActorSystem("NotificationSendingCronSpec"))
  with ImplicitSender with WordSpecLike with MustMatchers with BeforeAndAfterAll with MockitoSugar with Eventually {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val mailRecipient1 = MailRecipient("user1")
  val mailRecipient2 = MailRecipient("user2")

  "NotificationSending" should {
    "send to 1 link to 2 users" in {
      //given
      val repository = new SendOrdersRepository {
        override def add(users: Seq[MailRecipient], url: String): Future[Boolean] = ???
        override def allReadyToSend(): Future[Seq[SendOrders]] = Future.successful {
          Seq(
            SendOrders(id = Some(1), url = "url1", mailRecipient1, new Timestamp(1), false),
            SendOrders(id = Some(2), url = "url2", mailRecipient2, new Timestamp(1), false)
          )
        }
        override def markAsSent(ids: Seq[Int]): Future[Boolean] = Future.successful(true)
      }


      val mailer = mock[MailSender]
      when(mailer.send(mailRecipient1, "url1")).thenReturn(Future.successful(MailSent))
      when(mailer.send(mailRecipient2, "url2")).thenReturn(Future.successful(MailSent))

      //when
      val cron = system.actorOf(NotificationSendingCron.props(repository, mailer))
      cron ! SendNow

      //then
      eventually {
        verify(mailer).send(mailRecipient1, "url1")
        verify(mailer).send(mailRecipient2, "url2")
      }
    }

    "send to 2 links to 1 users" in {
      //given
      val repository = new SendOrdersRepository {
        override def add(users: Seq[MailRecipient], url: String): Future[Boolean] = ???
        override def allReadyToSend(): Future[Seq[SendOrders]] = Future.successful {
          Seq(
            SendOrders(id = Some(1), url = "url1", mailRecipient1, new Timestamp(1), false),
            SendOrders(id = Some(2), url = "url2", mailRecipient1, new Timestamp(1), false)
          )
        }
        override def markAsSent(ids: Seq[Int]): Future[Boolean] = Future.successful(true)
      }


      val mailer = mock[MailSender]
      when(mailer.send(mailRecipient1, "url1 \n url2")).thenReturn(Future.successful(MailSent))

      //when
      val cron = system.actorOf(NotificationSendingCron.props(repository, mailer))
      cron ! SendNow

      //then
      eventually {
        verify(mailer).send(mailRecipient1, "url1 \n url2")
      }
    }

    "retry on error" in {
      //given
      val repository = new SendOrdersRepository {
        override def add(users: Seq[MailRecipient], url: String): Future[Boolean] = ???
        override def allReadyToSend(): Future[Seq[SendOrders]] = Future.successful {
          Seq(
            SendOrders(id = Some(1), url = "url1", mailRecipient1, new Timestamp(1), false),
            SendOrders(id = Some(2), url = "url2", mailRecipient1, new Timestamp(1), false)
          )
        }
        override def markAsSent(ids: Seq[Int]): Future[Boolean] = Future.successful(true)
      }


      val mailer = mock[MailSender]
      when(mailer.send(mailRecipient1, "url1 \n url2")).thenReturn(Future.failed(new Exception("Did not send!")))

      //when
      val cron = system.actorOf(NotificationSendingCron.props(repository, mailer))
      cron ! SendNow
      cron ! SendNow
      cron ! SendNow // we try again later

      //then
      eventually {
        verify(mailer, times(3)).send(mailRecipient1, "url1 \n url2")
      }
    }
  }
}
