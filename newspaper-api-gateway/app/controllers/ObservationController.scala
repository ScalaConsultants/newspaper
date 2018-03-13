package controllers

import java.net.{MalformedURLException, URL}
import javax.inject.Inject

import model.PageToObserve
import model.Subscriber._
import play.api.libs.json.{JsSuccess, JsValue}
import play.api.mvc._
import services.OutboundService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class ObservationController @Inject()(
       out: OutboundService,
       cc: ControllerComponents) extends AbstractController(cc) {

  def add() = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[PageToObserve] match {
      case JsSuccess(obs, _) if checkUrlValidness(obs.pageUrl).getOrElse(false) =>
        val prefixedObserver = if(obs.pageUrl.contains("http"))
            obs
          else
            obs.copy(pageUrl = "http://" + obs.pageUrl)
        out.publishNewObservedPage(prefixedObserver).map { _ =>
          Ok("{}")
        }
      case JsSuccess(_, _)  =>
        Future.successful(BadRequest("{'error': 'pageUrl value is not correct'}"))
      case _ =>
        Future.successful(BadRequest("{'error': 'json is malformed'}"))
    }
  }

  private def checkUrlValidness(url: String) = Try {
    new URL(url)
    true
  }.recover {
    case _:MalformedURLException => false
  }
}
