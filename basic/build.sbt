name := "freestyle-cassandra-low-level-api"

scalaVersion := "2.12.3"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("tabdulradi", "maven")
)

addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "io.frees" %% "frees-async"             % "0.4.1",
  "io.frees" %% "frees-async-cats-effect" % "0.4.1",
  "io.frees" %% "frees-monix"             % "0.4.1",
  "io.frees" %% "frees-logging"           % "0.4.1",
  "io.frees" %% "frees-cassandra-core"    % "0.0.3"
)
