name := "newspaper-crawler"

version := "1.0"

organization := "io.scalac"

scalaVersion := "2.11.11"

scalacOptions ++= Seq (
  "-feature",
  "-deprecation",
  "-language:postfixOps",
  "-language:implicitConversions",
  "UTF-8"
)

resolvers += Resolver.jcenterRepo

libraryDependencies ++= {

  val newspaperSchemaV = "0.1.0-SNAPSHOT"

  val akkaV = "2.5.2"
  val scalatestV = "3.0.1"
  val playWSV = "1.0.0-M10"
  val akkaStreamsKafkaV = "0.16"
  val configsV = "0.4.4"
  val logbackV = "1.1.7"
  val scalaLoggingV = "3.5.0"
  val levelDbV = "0.7"
  val leveldbjniV = "1.8"
  val persistenceInMemV = "1.3.7"

  Seq (
    "io.scalac" %% "newspaper-schema" % newspaperSchemaV,

    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-persistence" % akkaV,
    "com.typesafe.play" %% "play-ahc-ws-standalone" % playWSV,
    "com.typesafe.akka" %% "akka-stream-kafka" % akkaStreamsKafkaV,
    "com.github.kxbmap" %% "configs" % configsV,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV,
    "ch.qos.logback" % "logback-classic" % logbackV,
    "org.iq80.leveldb" % "leveldb" % levelDbV,
    "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbjniV,

    "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    "org.scalatest" %% "scalatest" % scalatestV % "test",
    "com.github.dnvriend" %% "akka-persistence-inmemory" % persistenceInMemV % "test"
  )
}