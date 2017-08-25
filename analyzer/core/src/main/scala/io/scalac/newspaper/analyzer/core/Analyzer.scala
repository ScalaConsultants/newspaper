package io.scalac.newspaper.analyzer.core

trait Analyzer {

  def checkForChanges(oldContentOption: Option[PageContent], newContent: PageContent): List[Change]

}

class SimpleAnalyzer extends Analyzer {

  private val commentPattern = """(?s)<!--.*?-->""".r
  private val tagPattern = """(?s)<(/?)\s*([a-zA-Z0-1-]+)[^>]*>""".r
  private val scriptPattern = """(?s)<script>(.*?)</script>""".r
  private val bodyPattern = """(?s)<body>(.*)</body>""".r

  def checkForChanges(oldContentOption: Option[PageContent], newContent: PageContent): List[Change] =
    oldContentOption match {
      case None =>
        // This is the initial fetch, so no change generated
        List()

      case Some(oldContent) =>
        val oldPreprocessed = preprocess(oldContent)
        val newPreprocessed = preprocess(newContent)

        if (oldPreprocessed != newPreprocessed) {
          // Change detected
          List(Change(newContent.content))
        } else {
          // Nothing changed
          List()
        }
    }

  def preprocess(fullContent: PageContent): PageContent = {
    val commentStripped = commentPattern.replaceAllIn(fullContent.content, "")
    val tagStripped = tagPattern.replaceAllIn(commentStripped, m => s"""<${m.group(1)}${m.group(2)}>""")
    val scriptStripped = scriptPattern.replaceAllIn(tagStripped, """<script></script>""")
    val preprocessed = bodyPattern.findFirstIn(scriptStripped) match {
      case Some(body) => body
      case None       => scriptStripped
    }

    PageContent(preprocessed)
  }

}
