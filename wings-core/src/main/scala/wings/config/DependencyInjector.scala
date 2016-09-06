package wings.config

import java.net.URI

import reactivemongo.api.DB
import scaldi.Injectable._

import scala.collection.JavaConverters._
import scaldi.Module
import wings.hexagonal.virtualobject.application.usecase.ShowVirtualObject
import wings.hexagonal.virtualobject.domain.repository.VirtualObjectRepository
import wings.hexagonal.virtualobject.infrastructure.repository.VirtualObjectRepositoryImpl
import wings.model.virtual.virtualobject.VOIdentityManager
import wings.model.virtual.virtualobject.services.db.mongo.VirtualObjectMongoService
import wings.services.db.{MongoEnvironment, MongoEnvironmentImpl}

import scala.concurrent.ExecutionContext.Implicits.global

object DependencyInjector {

  def coreInjector = new Module {

    bind[URI] identifiedBy 'MqttBroker to Config.config.getStringList("mqtt.servers").asScala.map(new URI(_)).head

    bind[List[String]] identifiedBy 'MongoDBServers to Config.config.getStringList("mongodb.servers").asScala.toList

    bind[String] identifiedBy 'MainMongoDatabase to Config.config.getString("mongodb.db")

    bind[MongoEnvironment] identifiedBy 'MongoEnvironment to MongoEnvironmentImpl

    bind[DB] identifiedBy 'mainDb to inject[MongoEnvironment](identified by 'MongoEnvironment).mainDb


    /**
      * VirtualObject
      */

    bind[VirtualObjectMongoService] identifiedBy 'VirtualObjectMongoService to VirtualObjectMongoService(inject[DB](identified by 'mainDb))(VOIdentityManager)

    bind[VirtualObjectRepository] identifiedBy 'VirtualObjectRepository to VirtualObjectRepositoryImpl(inject[VirtualObjectMongoService](identified by 'VirtualObjectMongoService))

    bind[ShowVirtualObject.UseCase] identifiedBy 'ShowVirtualObjectUseCase to ShowVirtualObject.UseCase(inject[VirtualObjectRepository](identified by 'VirtualObjectRepository))

  }

  implicit val implicitCoreInjector = coreInjector

}
