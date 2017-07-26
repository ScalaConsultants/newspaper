package io.scalac.newspaper.analyzer.kafka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{ Producer, Consumer }
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer}

import io.scalac.newspaper.analyzer.core._
import io.scalac.newspaper.analyzer.db.postgres._
import AnalyzerFlow._

object AnalyzerRunner extends App {

  implicit val system = ActorSystem("Newspaper-Analyzer-System")
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val maxParallelism = config.getInt("maxParallelism")

  val logger = Logger("analyzer")

  val archive  = new PostgresArchive
  val analyzer = new Analyzer

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new ContentFetchedDeserializer)
    .withGroupId("Newspaper-Analyzer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new ChangeDetectedSerializer)

  val subscription = Subscriptions.topics("newspaper-content")

  val contentFetchedSource: Source[ContentFetchedCommittableMessage, Control] = Consumer.committableSource(consumerSettings, subscription)

  val analyzerFlow = AnalyzerFlow(analyzer, archive, logger, maxParallelism)

  val changeDetectedCommitFlow: Flow[ChangeDetectedMessage, Done, NotUsed] =
    Flow[ChangeDetectedMessage]
    .via(Producer.flow(producerSettings))
    .map(_.message.passThrough)
    .collect{ case Some(offset) => offset }
    .mapAsync(maxParallelism)(_.commitScaladsl())

  val runnable: RunnableGraph[Future[Done]] =
    RunnableGraph.fromGraph(GraphDSL.create(Sink.ignore) { implicit builder => ignore =>
      import GraphDSL.Implicits._

      val fetchContent = builder.add(contentFetchedSource)
      val analyze = builder.add(analyzerFlow)
      val commitChanges = builder.add(changeDetectedCommitFlow)

      fetchContent ~> analyze ~> commitChanges ~> ignore

      ClosedShape
    })

  val done = runnable.run()

  done.onComplete { _ =>
    logger.info("Shutting down...")
    system.terminate()
    archive.close()
  }

}
