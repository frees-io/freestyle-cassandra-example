name := "freestyle-cassandra-low-level-api"

scalaVersion := "2.12.2"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
    "io.frees" %% "freestyle-monix" % "0.3.0",
    "io.frees" %% "freestyle-async-monix" % "0.3.0",
    "io.frees" %% "freestyle-cassandra-core" % "0.0.1-SNAPSHOT",
    "io.frees" %% "freestyle-cassandra-macros" % "0.0.1-SNAPSHOT"
  )

addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M8" cross CrossVersion.full)
