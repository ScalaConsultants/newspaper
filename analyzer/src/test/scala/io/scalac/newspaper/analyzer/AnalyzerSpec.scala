package io.scalac.newspaper.analyzer

import org.scalatest._

class AnalyzerSpec extends WordSpecLike with Matchers {

  "Analyzer" should {
    val analyzer = new Analyzer
    val url1 = "http://example.com/foo"
    val content1a = "bar"
    val content1b = "baz bar"

    "not generate ChangeDetected when presented new page" in {
      val changes = analyzer.checkForChanges(url1, content1a)
      changes.size shouldEqual 0
    }

    "not generate ChangeDetected when presented exactly the same page" in {
      val changes = analyzer.checkForChanges(url1, content1a)
      changes.size shouldEqual 0
    }

    "generate single ChangeDetected when presented page with single addition" in {
      val changes = analyzer.checkForChanges(url1, content1b)
      changes.size shouldEqual 1
    }
  }

}
