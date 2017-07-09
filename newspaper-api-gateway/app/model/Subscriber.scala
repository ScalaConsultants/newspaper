package model

import play.api.libs.json.Json

case class Subscriber(email: String, name: Option[String])

case object Subscriber {
  implicit val format = Json.reads[Subscriber]
}