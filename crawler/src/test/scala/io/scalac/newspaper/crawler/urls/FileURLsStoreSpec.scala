package io.scalac.newspaper.crawler.urls

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Framing, Sink, Source}
import akka.testkit.TestKit
import akka.util.ByteString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.immutable
import scala.concurrent.Future

/**
  * Created by rsekulski on 04.07.2017.
  */
class FileURLsStoreSpec extends TestKit(ActorSystem("test-system")) with WordSpecLike with BeforeAndAfterAll with Matchers with ScalaFutures {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  "FileURLsStore" should {
    "be able to delete url from file" in {
      val path = Paths.get("src/test/resources/urls")
      val google = "http://google.pl"
      val scalac = "http://blog.scalac.io"

      val urls = immutable.Seq(google, scalac)

      val resultF = for {
        _ <- Source(urls)
          .map(s => ByteString(s + "\n"))
          .runWith(FileIO.toPath(path))
        cut <- Future.successful(new FileURLsStore(path))
        _ <- cut.removeURL(scalac)
        urlsFromStore <- cut.getURLs.runWith(Sink.seq)
        urlsFromFile <- FileIO.fromPath(path).via(Framing.delimiter(ByteString("\n"), 1024, true)).map(_.utf8String).runWith(Sink.seq)
      } yield(urlsFromFile, urlsFromStore)

      val (urlsFromFile, urlsFromStore) = resultF.futureValue

      urlsFromFile shouldEqual urlsFromStore
      urlsFromFile shouldEqual Vector(google)

    }
  }
}
