import sbt._

object Dependencies {

  lazy val akkaVersion = "2.4.9"

  lazy val playVersion = "2.5.6"

  lazy val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion

  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  lazy val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion

  lazy val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion

  lazy val akkaClusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion

  lazy val akkaTestKit = "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion

  lazy val akkaSlf4j = "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.4"

  lazy val playJson = "com.typesafe.play" %% "play-json" % playVersion

  lazy val playWs = "com.typesafe.play" %% "play-ws" % playVersion

  lazy val playScalaTest = "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"

  lazy val playReactiveMongo = "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14"

  lazy val playReactiveMongo24 = "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14-play24"

  lazy val slf4j = "org.slf4j" % "slf4j-api" % "1.7.21"

  lazy val eclipsePaho = "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"

  lazy val jettyWebSocket = "org.eclipse.jetty.websocket" % "websocket-client" % "9.3.8.v20160314"

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.3.0"

  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"

  lazy val logback = "ch.qos.logback" %  "logback-classic" % "1.1.7"

  lazy val scalatest = "org.scalatest" %% "scalatest" % "2.2.6"

  lazy val scalactic = "org.scalactic" %% "scalactic" % "2.2.6"

  lazy val scaldi = "org.scaldi" %% "scaldi" % "0.5.7"

}

