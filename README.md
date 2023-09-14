# Wings

Distributed application bridging Internet of Things and Web of Things featuring the actor model with Akka. Related scientific manuscripts: https://www.mdpi.com/1424-8220/18/2/400

This project is organized in a monorepo style and aggregates several sub-projects. The main sub-projects are:

* wings-core
* wings-http
* wings-mqtt
* wings-clusterseed
* wings-test

wings-core, wings-http and wings-mqtt are the main projects. wings-core contains the business logic for both wings-http and wings-mqtt, acting as the domain and application layer (mainly), and defines actors with such logic. It also defines actors that are aware of the PubSub cluster and communication model.
Actors are used as layers to provide fault-tolerance and actor restart in case of failure. wings-http and wing-mqtt are the adapter/infrastructure layer, implementing the interface between specific IoT protocols.

## wings-core

This project contains the core logic, mainly business logic. It defines several interfaces and core actors and passes messages between adapter actors (http, websocket, mqtt) and the PubSubCluster (wings-clusterseed).

## wings-http

This project contains adapters for WebSocket and HTTP via the PlayFramework 2. PlayFramework 2 handles initial WebSocket connections and transforms them into actors by creating an actor per connection (IoT device or client),
It also contains the HTTP client to upgrade HTTP connections to WebSocket connections and to display, via HTML, CSS and Javascript, a dashboard and a map.

## wings-mqtt

This project contains adapters for MQTT via Eclipse Paho.

## wings-clusterseed

Contains the seeds and initialization code to create a distributed PubSub cluster with Akka cluster.

## wings-test

Contains e2e tests.

## Packaging

All projects share the same packaging plugin, that is [sbt-native-packager](https://github.com/sbt/sbt-native-packager)

In order to package a project, enter the sbt console of the project and then issue `universal:packageBin`
