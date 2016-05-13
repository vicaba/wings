import play.sbt._
import play.sbt.PlayImport._
import play.sbt.routes.RoutesKeys._

import sbt.Keys._
import sbt._

import Dependencies._

object WingsBuild extends Build {

  lazy val commonSettings = Seq(
    organization := "LaSalle",
    scalaVersion := "2.11.8",
    version := "1.0"
  )

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

  lazy val coreDependencies = Seq(libraryDependencies ++= Seq(
    akkaCluster,
    akkaClusterTools,
    akkaClusterMetrics,
    playJson,
    playReactiveMongo
  ))

  lazy val http = Project(
    id = "wings-http",
    base = file("wings-http"),
    settings = commonSettings,
    dependencies = Seq(core)
  )
    .enablePlugins(PlayScala)
    .settings(httpDependencies: _*)
    .settings(routesGenerator := InjectedRoutesGenerator, fork in sbt.Keys.run := true)

  lazy val httpDependencies = Seq(libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws,
    specs2 % Test,
    playReactiveMongo,
    akkaRemote
  ))

  lazy val mqtt = Project(
    id = "wings-mqtt",
    base = file("wings-mqtt"),
    settings = commonSettings,
    dependencies = Seq(core)
  )
    .settings(mqttDependencies:_*)

  lazy val mqttDependencies = Seq(libraryDependencies ++= Seq(
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
    settings = commonSettings
  )
    .settings(clusterSeedDependencies:_*)

  lazy val clusterSeedDependencies = Seq(libraryDependencies ++= Seq(
    akkaCluster,
    akkaClusterTools,
    akkaClusterMetrics
  ))

}