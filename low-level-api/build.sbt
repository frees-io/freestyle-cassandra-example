name := "freestyle-cassandra-low-level-api"

scalaVersion := "2.12.2"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
    "io.frees" %% "freestyle-monix" % "0.2.0",
    "io.frees" %% "freestyle-async-monix" % "0.2.0",
    "io.frees" %% "freestyle-cassandra-core" % "0.0.1-SNAPSHOT")