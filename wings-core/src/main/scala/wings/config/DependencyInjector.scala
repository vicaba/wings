package wings.config

import java.net.URI

import scala.collection.JavaConverters._
import scaldi.Module
import wings.services.db.{MongoEnvironment, MongoEnvironmentImpl}

object DependencyInjector {

  implicit val injector = new Module {

    bind[URI] identifiedBy 'MqttBroker to Config.config.getStringList("mqtt.servers").asScala.map(new URI(_)).head

    bind[List[String]] identifiedBy 'MongoDBServers to Config.config.getStringList("mongodb.servers").asScala.toList

    bind[String] identifiedBy 'MainMongoDatabase to Config.config.getString("mongodb.db")

    bind[MongoEnvironment] identifiedBy 'MongoEnvironment to MongoEnvironmentImpl

  }

}
