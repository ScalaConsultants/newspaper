package io.scalac.newspaper.crawler

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import io.scalac.newspaper.crawler.PageContentRefresher.StartFetching
import io.scalac.newspaper.crawler.fetching.FetchingProcess

object PageContentRefresher {
  def props(fetchingProcess: FetchingProcess)(implicit materializer: ActorMaterializer): Props = Props(new PageContentRefresher(fetchingProcess))

  case object StartFetching
}

class PageContentRefresher(fetchingProcess: FetchingProcess)(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {
  override def receive: Receive = {
    case StartFetching => {
      log.info("Started fetching")
      fetchingProcess.process.run()
    }
  }
}
