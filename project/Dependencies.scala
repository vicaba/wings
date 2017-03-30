import sbt._

object Dependencies {

  val akkaVersion: String = "2.4.10"

  val playVersion: String = "2.5.6"

  val akkaRemote: ModuleID = "com.typesafe.akka" %% "akka-remote" % akkaVersion

  val akkaActor: ModuleID = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  val akkaCluster: ModuleID = "com.typesafe.akka" %% "akka-cluster" % akkaVersion

  val akkaClusterTools: ModuleID = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion

  val akkaClusterMetrics: ModuleID = "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion

  val akkaTestKit: ModuleID = "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion

  val akkaSlf4j: ModuleID = "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion

  val playJson: ModuleID = "com.typesafe.play" %% "play-json" % playVersion

  val playWs: ModuleID = "com.typesafe.play" %% "play-ws" % playVersion

  val playScalaTest: ModuleID = "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"

  val playReactiveMongo: ModuleID = "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14"

  val playReactiveMongo24: ModuleID = "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14-play24"

  val slf4j: ModuleID = "org.slf4j" % "slf4j-api" % "1.7.21"

  val eclipsePaho: ModuleID = "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.1.0"

  val jettyWebSocket: ModuleID = "org.eclipse.jetty.websocket" % "websocket-client" % "9.3.11.v20160721"

  val typesafeConfig: ModuleID = "com.typesafe" % "config" % "1.3.0"

  val scalaLogging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"

  val logback: ModuleID = "ch.qos.logback" % "logback-classic" % "1.1.7"

  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.0"

  val scalactic: ModuleID = "org.scalactic" %% "scalactic" % "3.0.0"

  val scaldi: ModuleID = "org.scaldi" %% "scaldi" % "0.5.7"

}
