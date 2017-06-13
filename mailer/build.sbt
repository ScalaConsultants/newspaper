name := "newspaper-mailer"

version := "1.0"

scalaVersion := "2.11.11"

lazy val akkaVersion = "2.5.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.14",
  "ch.lightshed" %% "courier" % "0.1.4",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

resolvers += "lightshed-maven" at "http://dl.bintray.com/content/lightshed/maven"
