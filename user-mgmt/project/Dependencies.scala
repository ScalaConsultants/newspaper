import sbt._

object Dependencies {
  lazy val akkaVersion = "2.5.2"
  lazy val akkaDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.14",
    "com.typesafe" % "config" % "1.3.1"
  )
  lazy val schemaDependency = "io.scalac" %% "newspaper-schema" % "0.1.0-SNAPSHOT"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"
}
