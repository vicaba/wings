/*
import play.sbt._
import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys._
import sbt.Keys._
import sbt._
import Dependencies._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

object WingsBuild extends Build {

  lazy val commonSettings = Configuration.settings

  lazy val aggregatedProjects: Seq[ProjectReference] = Seq(core, http, mqtt, clusterSeed)

  lazy val root = Project(
    id = "wings",
    base = file("."),
    aggregate = aggregatedProjects,
    settings = commonSettings
  )

  lazy val scalaGraph = Project(
    id = "scala-graph",
    base = file("scala-graph")
  )

  lazy val core = Project(
    id = "wings-core",
    base = file("wings-core"),
    settings = commonSettings,
    dependencies = Seq(scalaGraph)
  ).settings(coreDependencies: _*)

  lazy val coreDependencies = Seq(
    libraryDependencies ++= Seq(
      scaldi,
      akkaCluster,
      akkaClusterTools,
      akkaClusterMetrics,
      playJson,
      playReactiveMongo,
      akkaSlf4j,
      scalaLogging,
      logback,
      scalactic
    ))

  lazy val http = Project(
    id = "wings-http",
    base = file("wings-http"),
    settings = commonSettings,
    dependencies = Seq(core)
  ).enablePlugins(PlayScala)
    .settings(httpDependencies: _*)
    .settings(routesGenerator := InjectedRoutesGenerator, fork in sbt.Keys.run := false)

  lazy val httpDependencies = Seq(
    libraryDependencies ++= Seq(
      jdbc,
      cache,
      ws,
      filters,
      playReactiveMongo,
      akkaRemote
    ))

  lazy val mqtt = Project(
    id = "wings-mqtt",
    base = file("wings-mqtt"),
    settings = commonSettings,
    dependencies = Seq(core)
  ).settings(mqttDependencies: _*)
    .enablePlugins(JavaAppPackaging)

  lazy val mqttDependencies = Seq(
    libraryDependencies ++= Seq(
      akkaActor,
      akkaRemote,
      akkaCluster,
      akkaClusterTools,
      akkaClusterMetrics,
      playReactiveMongo,
      playJson,
      slf4j,
      eclipsePaho
    ))

  lazy val clusterSeed = Project(
    id = "wings-clusterseed",
    base = file("wings-clusterseed"),
    settings = commonSettings,
    dependencies = Seq(core)
  ).settings(clusterSeedDependencies: _*)
    .enablePlugins(JavaAppPackaging)

  lazy val clusterSeedDependencies = Seq(
    libraryDependencies ++= Seq(
      akkaCluster,
      akkaClusterTools,
      akkaClusterMetrics
    ))

  lazy val test = Project(
    id = "wings-test",
    base = file("wings-test"),
    settings = commonSettings,
    dependencies = Seq(core, mqtt)
  ).settings(testDependencies)
    .enablePlugins(JavaAppPackaging)
    .settings(mainClass in (Compile) := Some("wings.demo1.Demo1"))

  lazy val testDependencies = Seq(
    libraryDependencies ++= Seq(
      playWs,
      playJson,
      eclipsePaho,
      akkaActor,
      akkaRemote,
      akkaTestKit,
      playReactiveMongo24,
      typesafeConfig,
      jettyWebSocket,
      akkaSlf4j,
      scalatest
    ))

  /** ********* COMMANDS ALIASES ******************/
  addCommandAlias("t", "test")
  addCommandAlias("to", "testOnly")
  addCommandAlias("tq", "testQuick")
  addCommandAlias("tsf", "testShowFailed")

  addCommandAlias("c", "compile")
  addCommandAlias("tc", "test:compile")

  addCommandAlias("s", "scalastyle")
  addCommandAlias("ts", "test:scalastyle")

  addCommandAlias("f", "scalafmt")      // Format all files according to ScalaFmt
  addCommandAlias("ft", "scalafmtTest") // Test if all files are formatted according to ScalaFmt

  addCommandAlias("prep", ";c;tc;s;ts;ft") // All the needed tasks before running the test
}
*/
