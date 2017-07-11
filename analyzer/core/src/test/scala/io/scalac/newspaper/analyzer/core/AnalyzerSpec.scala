package io.scalac.newspaper.analyzer.core

import org.scalatest._

class AnalyzerSpec extends WordSpecLike with Matchers {

  "Analyzer" should {
    val analyzer = new Analyzer

    val content1a = PageContent("bar")
    val content1b = PageContent("baz bar")

    val content2a = PageContent("""<html><head>foo</head><body>baz<!--comment--><script>var x = 1;</script></body></html>""")
    val content2b = PageContent("""<html><head>bar</head><body>baz<!--comment--><script>var x = 1;</script></body></html>""")
    val content2c = PageContent("""<html><head>bar</head><body>baz<!--ignored--><script>var x = 1;</script></body></html>""")
    val content2d = PageContent("""<html><head>bar</head><body>baz<!--ignored--><script>var x = 2;</script></body></html>""")
    val content2e = PageContent("""<html><head>bar</head><body class="ignored">baz<!--ignored--><script>var x = 2;</script></body></html>""")

    "not generate ChangeDetected when presented new page" in {
      val changes = analyzer.checkForChanges(None, content1a)
      changes.size shouldEqual 0
    }

    "not generate ChangeDetected when presented exactly the same page" in {
      val changes = analyzer.checkForChanges(Some(content1a), content1a)
      changes.size shouldEqual 0
    }

    "generate single ChangeDetected when presented page with single addition" in {
      val changes = analyzer.checkForChanges(Some(content1a), content1b)
      changes.size shouldEqual 1
    }

    "ignore changes outside <body>" in {
      val changes = analyzer.checkForChanges(Some(content2a), content2b)
      changes.size shouldEqual 0
    }

    "ignore <!--comments-->" in {
      val changes = analyzer.checkForChanges(Some(content2b), content2c)
      changes.size shouldEqual 0
    }

    "ignore scripts" in {
      val changes = analyzer.checkForChanges(Some(content2c), content2d)
      changes.size shouldEqual 0
    }

    "ignore element attributes" in {
      val changes = analyzer.checkForChanges(Some(content2d), content2e)
      changes.size shouldEqual 0
    }

  }

}
