# Wings

Distributed application bridging Internet of Things and Web of Things featuring the actor model with Akka. Related scientific manuscripts: https://www.mdpi.com/1424-8220/18/2/400

This project aggregates several sub-projects. They are:

* wings-http
* wings-mqtt
* wings-test
* wings-clusterseed

## Packaging

All projects share the same packaging plugin, that is [sbt-native-packager](https://github.com/sbt/sbt-native-packager)

In order to package a project, enter the sbt console of the project and then issue `universal:packageBin`
