package io.scalac.newspaper.crawler

import akka.actor.{Actor, Props}
import io.scalac.newspaper.crawler.PageContentRefresher.StartFetching
import io.scalac.newspaper.crawler.fetching.FetchingProcess

object PageContentRefresher {
  def props(fetchingProcess: FetchingProcess): Props = Props(new PageContentRefresher(fetchingProcess))

  case object StartFetching
}

class PageContentRefresher(fetchingProcess: FetchingProcess) extends Actor {
  override def receive: Receive = {
    case StartFetching => println("Let the fetching begin!")
  }
}
