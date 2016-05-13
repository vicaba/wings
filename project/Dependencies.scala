import sbt._

object Dependencies {

  val akkaVersion = "2.4.4"

  val playVersion = "2.5.3"

  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion

  val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion

  val akkaClusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion

  val playJson = "com.typesafe.play" %% "play-json" % playVersion

  val playReactiveMongo = "org.reactivemongo" %% "play2-reactivemongo" % "0.11.2.play24"

  val slf4j = "org.slf4j" % "slf4j-simple" % "1.7.21"

  val eclipsePaho = "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"
}

