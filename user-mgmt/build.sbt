import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.scalac",
      scalaVersion := "2.11.11",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "newspaper-user-mgmt",
    libraryDependencies ++= (akkaDependencies :+ schemaDependency),
    libraryDependencies ++= dbDependencies,
    libraryDependencies += scalaTest % Test
  )

flywayUrl := "jdbc:postgresql://192.168.99.100:5432/user_mgmt_db" //TODO: how to provide host?

flywayUser := "user_mgmt" //set in db-scripts, a top level script
flywayPassword := "user_mgmt123"