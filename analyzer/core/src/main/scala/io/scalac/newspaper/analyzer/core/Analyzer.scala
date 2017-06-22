package io.scalac.newspaper.analyzer.core

import scala.collection.mutable

class Analyzer {

  import Analyzer._

  private val archive: mutable.Map[String, PreprocessedContent] = new mutable.HashMap()

  def checkForChanges(url: String, fullContent: String): List[Change] = {
    val newContent = PreprocessedContent(fullContent)

    archive.get(url) match {
      case None =>
        // This is the initial fetch, so no change generated
        archive.put(url, newContent)
        List()

      case Some(oldContent) =>
        if (oldContent != newContent) {
          // Change detected
          archive.put(url, newContent)
          List(Change(fullContent))
        } else {
          // Nothing changed
          List()
        }
    }
  }

}

object Analyzer {
  case class PreprocessedContent(content: String)
  case class Change(content: String)
}
