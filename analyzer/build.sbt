name := "newspaper-analyzer"

version := "1.0"

scalaVersion := "2.11.11"

lazy val akkaVersion = "2.5.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.14",
  "io.scalac" %% "newspaper-schema" % "0.1.0-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)
