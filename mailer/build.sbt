name := "newspaper-mailer"

version := "1.0"

scalaVersion := "2.11.11"

lazy val akkaVersion = "2.5.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % "2.5.2",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.14",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
