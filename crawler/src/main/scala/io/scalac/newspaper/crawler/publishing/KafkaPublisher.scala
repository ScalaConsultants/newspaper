package io.scalac.newspaper.crawler.publishing

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import events.ContentFetched
import io.scalac.newspaper.crawler.fetching.FetchingFlow.URLFetched
import io.scalac.newspaper.crawler.publishing.KafkaPublisher._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer

import scala.concurrent.Future


trait KafkaPublisher extends Publisher {

  def system: ActorSystem
  def topic: String
  private def producerSettings = ProducerSettings(system, new ByteArraySerializer, new ContentFetchedSerializer)

  override def publish: Sink[URLFetched, Future[Done]] =
    Flow[URLFetched]
      .map(new ProducerRecord[Array[Byte], ContentFetched](topic, _))
      .toMat(Producer.plainSink[Array[Byte], ContentFetched](producerSettings))(Keep.right)
}

object KafkaPublisher {
  implicit def urlFetched2ContentFetched(source: URLFetched): ContentFetched =
    ContentFetched(source.url, source.content)
}