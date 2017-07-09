package services

import javax.inject.Singleton

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import com.google.inject.ImplementedBy
import io.scalac.newspaper.events.SubscribeUser
import model.Subscriber
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer}
import serializers.SubscribeUserPBSerializer

import scala.concurrent.Future

@ImplementedBy(classOf[KafkaOutboundService])
trait OutboundService {
  def publishNewSubscription(s: Subscriber): Future[Unit]
}

@Singleton
class KafkaOutboundService() extends OutboundService {
  //TODO: move it outside and share among services
  implicit val system = ActorSystem("Newspaper-Analyzer-System")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new SubscribeUserPBSerializer)

  val source: SourceQueueWithComplete[SubscribeUser] =
    Source.queue[SubscribeUser](100, OverflowStrategy.fail).map { sub =>
      new ProducerRecord[Array[Byte], SubscribeUser]("newspaper-users", sub)
    }
    .to(Producer.plainSink(producerSettings))
    .run()

  override def publishNewSubscription(sub: Subscriber): Future[Unit] = {
    val domainObject = SubscribeUser(sub.email, sub.name.getOrElse(""))

    source.offer(domainObject).map{ _ =>
      ()
    }
  }
}