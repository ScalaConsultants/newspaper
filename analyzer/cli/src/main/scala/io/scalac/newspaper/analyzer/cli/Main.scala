package io.scalac.newspaper.analyzer.cli

import java.nio.file._
import java.nio.charset.StandardCharsets.UTF_8

import io.scalac.newspaper.analyzer.core.Analyzer

object Main extends App {

  def readFile(path: String): String = {
    new String(Files.readAllBytes(Paths.get(path)), UTF_8)
  }

  val analyzer = new Analyzer

  args match {
    case Array(oldFile, newFile) =>
      val oldContent = readFile(oldFile)
      val newContent = readFile(newFile)
      analyzer.checkForChanges("", oldContent)
      val changes = analyzer.checkForChanges("", newContent)
      println(changes)

    case _ =>
      println("Usage: analyzer old.html new.html")
      System.exit(1)
  }

}
