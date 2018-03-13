package services

import javax.inject.Singleton

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import com.google.inject.ImplementedBy
import io.scalac.newspaper.events.{ObservePage, SubscribeUser}
import model.{PageToObserve, Subscriber}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import serializers.{ObservePagePBSerializer, SubscribeUserPBSerializer}

import scala.concurrent.Future

@ImplementedBy(classOf[KafkaOutboundService])
trait OutboundService {
  def publishNewSubscription(s: Subscriber): Future[Unit]
  def publishNewObservedPage(o: PageToObserve): Future[Unit]
}

@Singleton
class KafkaOutboundService() extends OutboundService {
  //TODO: move it outside and share among services
  implicit val system = ActorSystem("Newspaper-Analyzer-System")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val usersProducerSettings = ProducerSettings(system, new ByteArraySerializer, new SubscribeUserPBSerializer)
  val usersSource: SourceQueueWithComplete[SubscribeUser] =
    Source.queue[SubscribeUser](100, OverflowStrategy.fail).map { sub =>
      new ProducerRecord[Array[Byte], SubscribeUser]("newspaper-users", sub)
    }
    .to(Producer.plainSink(usersProducerSettings))
    .run()

  val pagesProducerSettings = ProducerSettings(system, new ByteArraySerializer, new ObservePagePBSerializer)
  val pagesSource: SourceQueueWithComplete[ObservePage] =
    Source.queue[ObservePage](100, OverflowStrategy.fail).map { sub =>
      new ProducerRecord[Array[Byte], ObservePage]("newspaper-pages", sub)
    }
    .to(Producer.plainSink(pagesProducerSettings))
    .run()

  override def publishNewSubscription(sub: Subscriber): Future[Unit] = {
    val domainObject = SubscribeUser(sub.email, sub.name.getOrElse(""))

    usersSource.offer(domainObject).map{ _ =>
      ()
    }
  }

  override def publishNewObservedPage(o: PageToObserve): Future[Unit] = {
    val domainObject = ObservePage(o.pageUrl)

    pagesSource.offer(domainObject).map{ _ =>
      ()
    }
  }
}