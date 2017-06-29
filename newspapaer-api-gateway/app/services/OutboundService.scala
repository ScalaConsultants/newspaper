package services

import javax.inject.Singleton

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.google.inject.ImplementedBy
import model.Subscriber
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.Future

@ImplementedBy(classOf[KafkaOutboundService])
trait OutboundService {
  def publishNewSubscription(s: Subscriber): Future[Unit]
}

@Singleton
class KafkaOutboundService() extends OutboundService {
  implicit val system = ActorSystem("Newspaper-Analyzer-System")
  implicit val materializer = ActorMaterializer()

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)

  val source = Source(1 to 100)
  val done = source
    .map(_.toString)
    .map { elem =>
      new ProducerRecord[Array[Byte], String]("newspaper-users-foo", elem)
    }
    .runWith(Producer.plainSink(producerSettings))

  override def publishNewSubscription(s: Subscriber): Future[Unit] = Future.successful(println(s"Adding $s"))
}