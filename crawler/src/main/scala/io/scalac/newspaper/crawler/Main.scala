package io.scalac.newspaper.crawler

import java.nio.file.{Path, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import configs.Configs
import io.scalac.newspaper.crawler.fetching.HttpFetchingFlow.FetchingConfig
import io.scalac.newspaper.crawler.fetching.HttpURLFetcher.URLFetcherConfig
import io.scalac.newspaper.crawler.fetching.{FetchingProcess, HttpFetchingFlow, HttpURLFetcher}
import io.scalac.newspaper.crawler.publishing.KafkaPublisher
import io.scalac.newspaper.crawler.urls.FileURLsStore
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object Main extends App {
  that =>

  implicit val system = ActorSystem("crawler-system")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load

  val fetchingProcess = new FetchingProcess with FileURLsStore with HttpURLFetcher with HttpFetchingFlow with KafkaPublisher {
    override implicit val ec: ExecutionContext = that.ec
    override val system: ActorSystem = that.system

    override def urlFetcherConfig: HttpURLFetcher.URLFetcherConfig = URLFetcherConfig(Configs[Duration].get(config, "crawler.http.timeout").value)
    override def fetchingConfig: HttpFetchingFlow.FetchingConfig = FetchingConfig(config.getInt("crawler.http.max.parallelism"))

    override def wsClient: StandaloneWSClient = StandaloneAhcWSClient()

    override val topic: String = config.getString("kafka.topic")

    override def path: Path = Paths.get("src/main/resources/urls")
  }

  val pageContentRefresher = system.actorOf(PageContentRefresher.props(fetchingProcess))
  val scheduler = QuartzSchedulerExtension(system)

  scheduler.schedule("EverydayMidnight", pageContentRefresher, PageContentRefresher.StartFetching)


}
