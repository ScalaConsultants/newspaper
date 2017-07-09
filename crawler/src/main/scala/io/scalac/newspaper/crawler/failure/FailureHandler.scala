package io.scalac.newspaper.crawler.failure

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import io.scalac.newspaper.crawler.failure.FailureHandler._
import io.scalac.newspaper.crawler.fetching.FetchingFlow.{URLFetched, URLFetchingResult}
import io.scalac.newspaper.crawler.urls.URLsStore

import scala.collection.mutable


object FailureHandler {
  case class FailureReported(failure: URLFetchingResult)

  def props(urlsStore: URLsStore, config: FailureHandlerConfig = defaultConfig) = Props(new FailureHandler(urlsStore, config))

  case class FailureHandlerConfig(failuresLimit: Int, snapshotInterval: Int)
  val defaultConfig =  FailureHandlerConfig(3, 50)

  type FailuresStore = mutable.Map[String, Seq[URLFetchingResult]]

  case class GetFailures(url: String)
  case class URLFailures(failures: Seq[URLFetchingResult])
  case object Complete
}

class FailureHandler(urlsStore: URLsStore, config: FailureHandlerConfig) extends PersistentActor with ActorLogging {

  private var failures: FailuresStore = mutable.Map.empty

  override def persistenceId: String = "failure-handler"

  override def receiveCommand: Receive = {
    case GetFailures(url) => sender() ! URLFailures(failures.getOrElse(url, Seq.empty))

    case _:URLFetched => log.error("Received URLFetched in FailureHandler")

    case f:URLFetchingResult =>
      persist(FailureReported(f)) { failure =>
        log.info(s"Failure detected: $f")
        updateState(failure)
        removeNotActiveURL(f.url)
        if (lastSequenceNr % config.snapshotInterval == 0 && lastSequenceNr != 0) {
          saveSnapshot(failures)
          log.debug(s"Snapshot saved")
        }
      }

    case Complete => log.info("Stream processing complete")
  }

  override def receiveRecover: Receive = {
    case failure: FailureReported => updateState(failure)
    case SnapshotOffer(_, snapshot: FailuresStore) => failures = snapshot
  }

  private def updateState(failureReported: FailureReported): Unit = {
    val currentState = failures.getOrElse(failureReported.failure.url, Seq.empty[URLFetchingResult])
    failures.update(failureReported.failure.url, currentState :+ failureReported.failure)
  }

  private def removeNotActiveURL(url: String) = {
    if(failures.getOrElse(url, Seq.empty).size >= config.failuresLimit) {
      urlsStore.removeURL(url)
      log.info(s"Removed not active URL: $url")
    }
  }

}
