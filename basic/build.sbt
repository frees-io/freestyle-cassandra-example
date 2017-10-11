name := "freestyle-cassandra-low-level-api"

scalaVersion := "2.12.3"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "jitpack" at "https://jitpack.io")

addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "io.frees" %% "freestyle-monix"       % "0.3.1",
  "io.frees" %% "freestyle-async-monix" % "0.3.1",
  "io.frees" %% "freestyle-logging"     % "0.3.1",
  "io.frees" %% "frees-cassandra-core"  % "0.0.1-SNAPSHOT"
)
