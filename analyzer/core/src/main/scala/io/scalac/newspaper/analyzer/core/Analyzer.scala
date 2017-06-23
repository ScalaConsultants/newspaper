package io.scalac.newspaper.analyzer.core

import scala.collection.mutable

class Analyzer {

  import Analyzer._

  private val archive: mutable.Map[String, PreprocessedContent] = new mutable.HashMap()

  private val commentPattern = """(?s)<!--.*?-->""".r
  private val tagPattern = """(?s)<(/?)\s*([a-zA-Z0-1-]+)[^>]*>""".r
  private val scriptPattern = """(?s)<script>(.*?)</script>""".r
  private val bodyPattern = """(?s)<body>(.*)</body>""".r

  def checkForChanges(url: String, fullContent: String): List[Change] = {
    val newContent = preprocess(fullContent)

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

  def preprocess(fullContent: String): PreprocessedContent = {
    val commentStripped = commentPattern.replaceAllIn(fullContent, "")
    val tagStripped = tagPattern.replaceAllIn(commentStripped, m => s"""<${m.group(1)}${m.group(2)}>""")
    val scriptStripped = scriptPattern.replaceAllIn(tagStripped, """<script></script>""")
    val preprocessed = bodyPattern.findFirstIn(scriptStripped) match {
      case Some(body) => body
      case None       => scriptStripped
    }
    PreprocessedContent(preprocessed)
  }

}

object Analyzer {
  case class PreprocessedContent(content: String)
  case class Change(content: String)
}
