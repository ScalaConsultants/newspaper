package io.scalac.newspaper.crawler.fetching

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Flow, GraphDSL, Partition, RunnableGraph, Sink}
import io.scalac.newspaper.crawler.failure.FailureHandler
import io.scalac.newspaper.crawler.fetching.FetchingFlow.{URLFetched, URLFetchingResult}
import io.scalac.newspaper.crawler.publishing.Publisher
import io.scalac.newspaper.crawler.urls.URLsStore

trait FetchingProcess extends FetchingFlow with Publisher {

  def urlsStore: URLsStore
  def failureHandler: ActorRef

  def process = RunnableGraph.fromGraph(GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val inputURLs = builder.add(urlsStore.getURLs)
    val fetchingFlow = builder.add(fetchURLs)
    val responsesSplitter = builder.add(Partition(2, splitResponses))
    val publisher = builder.add(publish)
    val failureSink = builder.add(Sink.actorRef(failureHandler, FailureHandler.Complete))
    val toURLFetched = builder.add(urlFetchingResult2URLFetched)

    inputURLs ~> fetchingFlow ~> responsesSplitter
    responsesSplitter.out(0) ~> toURLFetched ~> publisher
    responsesSplitter.out(1) ~> failureSink
    ClosedShape
  })

  private def splitResponses: URLFetchingResult => Int = {
    case _: URLFetched => 0
    case _ => 1
  }

  private def urlFetchingResult2URLFetched: Flow[URLFetchingResult, URLFetched, NotUsed] =
    Flow[URLFetchingResult].map {
      case uf: URLFetched => uf
    }
}