package controllers

import javax.inject.Inject

import model.Subscriber
import model.Subscriber._
import play.api.data.validation.{Constraints, Valid}
import play.api.libs.json.{JsSuccess, JsValue}
import play.api.mvc._
import services.OutboundService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionController @Inject()(out: OutboundService, cc: ControllerComponents) extends AbstractController(cc) {
  def add() = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Subscriber] match {
      case JsSuccess(sub, _) if isValidEmail(sub.email) =>
        out.publishNewSubscription(sub).map(_ => Ok("{}"))
      case JsSuccess(_, _) =>
        Future.successful(BadRequest("{'error': 'email value is not correct'}"))
      case _ =>
        Future.successful(BadRequest("{'error': 'json is malformed'}"))
    }
  }

  private def isValidEmail(email: String): Boolean = Constraints.emailAddress.apply(email) == Valid
}
