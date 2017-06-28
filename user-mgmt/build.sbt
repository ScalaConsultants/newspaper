import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.scalac",
      scalaVersion := "2.11.11",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "user-mgmt",
    libraryDependencies ++= (akkaDependencies :+ schemaDependency),
    libraryDependencies += scalaTest % Test
  )
