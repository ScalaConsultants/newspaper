name := """newspaper-api-gateway"""
organization := "io.scalac"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

lazy val akkaVersion = "2.5.2"

libraryDependencies += guice
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.14"
libraryDependencies += "io.scalac" %% "newspaper-schema" % "0.1.0-SNAPSHOT"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "io.scalac.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "io.scalac.binders._"
