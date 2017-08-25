package io.scalac.newspaper.analyzer.cli

import java.nio.file._
import java.nio.charset.StandardCharsets.UTF_8

import io.scalac.newspaper.analyzer.core._

object Main extends App {

  def readFile(path: String): String = {
    new String(Files.readAllBytes(Paths.get(path)), UTF_8)
  }

  args match {
    case Array(oldFile, newFile) =>
      val oldContent = PageContent(readFile(oldFile))
      val newContent = PageContent(readFile(newFile))
      val analyzer = new SimpleAnalyzer()
      val newChanges = analyzer.checkForChanges(Some(oldContent), newContent)

      newChanges.foreach(println)

    case _ =>
      println("Usage: analyzer old.html new.html")
      System.exit(1)
  }

}
