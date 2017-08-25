// HACK: flyway-sbt seems to be broken, causing redownloading of dependencies
//       on each sbt reload.
//       Manually adding it to libraryDependencies instead of:
//         addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.2.0")
//       seems to work around this problem.
libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-sbt" % "4.2.0"
)

resolvers += "Flyway" at "https://flywaydb.org/repo"
