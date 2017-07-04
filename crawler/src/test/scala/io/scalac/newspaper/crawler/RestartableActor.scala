package io.scalac.newspaper.crawler

import akka.persistence.PersistentActor
import io.scalac.newspaper.crawler.RestartableActor.{RestartActor, RestartActorException}

/**
  * Created by rsekulski on 04.07.2017.
  */
trait RestartableActor extends PersistentActor {
  abstract override def receiveCommand = super.receiveCommand orElse {
    case RestartActor => throw RestartActorException
  }
}

object RestartableActor {
  case object RestartActor

  private object RestartActorException extends Exception
}
