package io.scalac.newspaper.crawler

import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import configs.Configs
import io.scalac.newspaper.crawler.failure.FailureHandler
import io.scalac.newspaper.crawler.fetching.HttpFetchingFlow.FetchingConfig
import io.scalac.newspaper.crawler.fetching.{FetchingProcess, HttpFetchingFlow}
import io.scalac.newspaper.crawler.publishing.KafkaPublisher
import io.scalac.newspaper.crawler.urls.FileURLsStore
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends App {
  that =>

  implicit val system = ActorSystem("crawler-system")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load

  val wsClient = StandaloneAhcWSClient()
  val topic = config.getString("kafka.topic")

  val fetchingProcess = new FetchingProcess with HttpFetchingFlow with KafkaPublisher {
    override implicit def ec: ExecutionContext = that.ec
    override implicit def system: ActorSystem = that.system

    override def fetchingConfig = FetchingConfig(
      config.getInt("crawler.http.max.parallelism"),
      getDuration(config, "crawler.http.timeout")
    )

    override def wsClient: StandaloneWSClient = that.wsClient

    override def topic: String = that.topic

    override val urlsStore = new FileURLsStore(Paths.get("src/main/resources/urls"))

    override val failureHandler: ActorRef = system.actorOf(FailureHandler.props(urlsStore), "FailureHandler")
  }

  val pageContentRefresher = system.actorOf(PageContentRefresher.props(fetchingProcess), "PageContentRefresher")

  system.scheduler.schedule(
    getDuration(config, "crawler.fetching.initial.delay"),
    getDuration(config, "crawler.fetching.interval"),
    pageContentRefresher,
    PageContentRefresher.StartFetching
  )


  private def getDuration(config: Config, path: String): FiniteDuration =
    Configs[FiniteDuration].get(config, path).value
}
