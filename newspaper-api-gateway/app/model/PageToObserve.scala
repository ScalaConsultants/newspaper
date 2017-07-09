package model

import play.api.libs.json.Json

case class PageToObserve(pageUrl: String)

case object PageToObserve {
  implicit val format = Json.reads[PageToObserve]
}
