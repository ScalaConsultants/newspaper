package io.scalac.newspaper.analyzer.core

import org.scalatest._

class AnalyzerSpec extends WordSpecLike with Matchers {

  "Analyzer" should {
    val analyzer = new Analyzer

    val url1 = "http://example.com/1"
    val content1a = "bar"
    val content1b = "baz bar"

    val url2 = "http://example.com/2"
    val content2a = """<html><head>foo</head><body>baz<!--comment--><script>var x = 1;</script></body></html>"""
    val content2b = """<html><head>bar</head><body>baz<!--comment--><script>var x = 1;</script></body></html>"""
    val content2c = """<html><head>bar</head><body>baz<!--ignored--><script>var x = 1;</script></body></html>"""
    val content2d = """<html><head>bar</head><body>baz<!--ignored--><script>var x = 2;</script></body></html>"""
    val content2e = """<html><head>bar</head><body class="ignored">baz<!--ignored--><script>var x = 2;</script></body></html>"""

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

    "not generate ChangeDetected when given a new URL" in {
      val changes = analyzer.checkForChanges(url2, content2a)
      changes.size shouldEqual 0
    }

    "ignore changes outside <body>" in {
      val changes = analyzer.checkForChanges(url2, content2b)
      changes.size shouldEqual 0
    }

    "ignore <!--comments-->" in {
      val changes = analyzer.checkForChanges(url2, content2c)
      changes.size shouldEqual 0
    }

    "ignore scripts" in {
      val changes = analyzer.checkForChanges(url2, content2d)
      changes.size shouldEqual 0
    }

    "ignore element attributes" in {
      val changes = analyzer.checkForChanges(url2, content2e)
      changes.size shouldEqual 0
    }

  }

}
