lazy val akkaVersion  = "2.5.2"
lazy val slickVersion = "3.2.0"

lazy val akkaActor       = "com.typesafe.akka"  %% "akka-actor"        % akkaVersion
lazy val akkaTestkit     = "com.typesafe.akka"  %% "akka-testkit"      % akkaVersion
lazy val akkaStream      = "com.typesafe.akka"  %% "akka-stream"       % akkaVersion
lazy val akkaStreamKafka = "com.typesafe.akka"  %% "akka-stream-kafka" % "0.14"
lazy val scalatest       = "org.scalatest"      %% "scalatest"         % "3.0.1"
lazy val schema          = "io.scalac"          %% "newspaper-schema"  % "0.1.0-SNAPSHOT"
lazy val slick           = "com.typesafe.slick" %% "slick"             % slickVersion
lazy val slickHikaricp   = "com.typesafe.slick" %% "slick-hikaricp"    % slickVersion
lazy val slf4j           = "org.slf4j"          %  "slf4j-nop"         % "1.6.4"
lazy val postgresql      = "org.postgresql"     %  "postgresql"        % "42.1.1"

lazy val root = project
  .in(file("."))
  .aggregate(core, kafka, cli)
  .settings(
    inThisBuild(List(
      organization := "io.scalac",
      scalaVersion := "2.11.11",
      version      := "0.1.0-SNAPSHOT",
      scalacOptions ++= Seq(
        "-feature",
        "-deprecation",
        "-unchecked",
        "-Xfuture",
        "-Yno-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen",
        "-Ywarn-value-discard",
        "-Ywarn-unused"
      )
    )),
    name := "newspaper-analyzer",
    run := (run in Compile in kafka).evaluated
  )

lazy val core = project
  .in(file("core"))
  .settings(
    name := "newspaper-analyzer-core",
    libraryDependencies ++= Seq(
      scalatest % "test"
    )
  )

lazy val dbPostgres = project
  .in(file("db-postgres"))
  .settings(
    name := "newspaper-analyzer-db-postgres",
    libraryDependencies ++= Seq(
      slick,
      slickHikaricp,
      slf4j,
      postgresql,
      scalatest % "test"
    ),
    flywayUrl := "jdbc:postgresql://127.0.0.1:5432/analyzer_db",
    flywayUser := "analyzer",
    flywayPassword := "analyzer123",
    flywayLocations := Seq("filesystem:db-postgres/src/main/resources/db/migration/")
  )
  .dependsOn(core)

lazy val kafka = project
  .in(file("kafka"))
  .settings(
    name := "newspaper-analyzer-kafka",
    libraryDependencies ++= Seq(
      akkaActor,
      akkaTestkit,
      akkaStream,
      akkaStreamKafka,
      schema,
      scalatest % "test"
    )
  )
  .dependsOn(core, dbPostgres)

lazy val cli = project
  .in(file("cli"))
  .settings(
    name := "newspaper-analyzer-cli"
  )
  .dependsOn(core)
