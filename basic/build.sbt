val appName = "freestyle-cassandra-examples"

scalaVersion := "2.12.3"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("tabdulradi", "maven")
)

val appPlugins = addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)

val dependencies = Seq(
  "io.frees" %% "frees-async"             % "0.4.1",
  "io.frees" %% "frees-async-cats-effect" % "0.4.1",
  "io.frees" %% "frees-monix"             % "0.4.1",
  "io.frees" %% "frees-logging"           % "0.4.1",
  "io.frees" %% "frees-cassandra-core"    % "0.0.3"
)

lazy val model = (project in file("model"))
  .settings(name := "model")
  .settings(libraryDependencies ++= dependencies)
  .settings(appPlugins)

lazy val examples = (project in file("examples"))
  .settings(name := "examples")
  .settings(libraryDependencies ++= dependencies)
  .settings(appPlugins)
  .dependsOn(model)

lazy val root = (project in file("."))
  .settings(name := appName)
  .aggregate(examples, model)
  .dependsOn(examples, model)