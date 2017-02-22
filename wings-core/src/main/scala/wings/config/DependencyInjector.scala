package wings.config

import java.net.URI

import reactivemongo.api.DB
import scaldi.Module
import wings.toolkit.db.mongodb.service.{MongoEnvironment, MongoEnvironmentImpl}
import wings.user.application.usecase.{SignInUser, SignUpUser}
import wings.user.domain.repository.UserRepository
import wings.user.infrastructure.repository.UserRepositoryImpl
import wings.user.infrastructure.repository.mongodb.UserMongoRepository
import wings.virtualobjectagent.domain.messages.event.repository.VirtualObjectSensedRepository
import wings.virtualobjectagent.infrastructure.messages.event.repository.VirtualObjectSensedRepositoryImpl
import wings.virtualobjectagent.infrastructure.messages.event.repository.mongodb.VirtualObjectSensedMongoRepository
import wings.virtualobject.application.usecase.{ListVirtualObject, ShowVirtualObject}
import wings.virtualobject.domain.repository.VirtualObjectRepository
import wings.virtualobject.infrastructure.repository.VirtualObjectRepositoryImpl
import wings.virtualobject.infrastructure.repository.mongodb.VirtualObjectMongoRepository
import wings.virtualobjectagent.application.usecase.ListVirtualObjectSensed
import wings.virtualobjectagent.domain.messages.event.VirtualObjectSensed

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

object DependencyInjector {

  def coreInjector = new Module {

    bind[URI] identifiedBy 'WebSocketServerWithPath to Config.config
      .getStringList("websocket.servers-with-path")
      .asScala
      .map(new URI(_))
      .head

    bind[URI] identifiedBy 'HttpServer to Config.config.getStringList("http.servers").asScala.map(new URI(_)).head

    bind[URI] identifiedBy 'MqttBroker to Config.config.getStringList("mqtt.servers").asScala.map(new URI(_)).head

    bind[List[String]] identifiedBy 'MongoDBServers to Config.config.getStringList("mongodb.servers").asScala.toList

    bind[String] identifiedBy 'MainMongoDatabase to Config.config.getString("mongodb.db")

    bind[MongoEnvironment] identifiedBy 'MongoEnvironment to MongoEnvironmentImpl

    bind[DB] identifiedBy 'mainDb to inject[MongoEnvironment](identified by 'MongoEnvironment).mainDb

    /**
      * VirtualObject
      */
    bind[VirtualObjectMongoRepository] identifiedBy 'VirtualObjectMongoService to VirtualObjectMongoRepository(
      inject[DB](identified by 'mainDb))

    bind[VirtualObjectRepository] identifiedBy 'VirtualObjectRepository to VirtualObjectRepositoryImpl(
      inject[VirtualObjectMongoRepository](identified by 'VirtualObjectMongoService))

    bind[ShowVirtualObject.UseCase] identifiedBy 'ShowVirtualObjectUseCase to ShowVirtualObject.UseCase(
      inject[VirtualObjectRepository](identified by 'VirtualObjectRepository))

    bind[ListVirtualObject.UseCase] identifiedBy 'ListVirtualObjectUseCase to ListVirtualObject.UseCase(
      inject[VirtualObjectRepository](identified by 'VirtualObjectRepository))

    /**
      * User
      */
    bind[UserMongoRepository] identifiedBy 'UserMongoRepository to UserMongoRepository(
      inject[DB](identified by 'mainDb))

    bind[UserRepository] identifiedBy 'UserRepository to UserRepositoryImpl(
      inject[UserMongoRepository](identified by 'UserMongoRepository))

    bind[SignInUser.UseCase] identifiedBy 'SignInUserUseCase to SignInUser.UseCase(
      inject[UserRepository](identified by 'UserRepository))

    bind[SignUpUser.UseCase] identifiedBy 'SignUpUserUseCase to SignUpUser.UseCase(
      inject[UserRepository](identified by 'UserRepository))

    /**
      * VirtualObjectSensed
      */
    bind[VirtualObjectSensedMongoRepository] identifiedBy 'VirtualObjectSensedMongoRepository to VirtualObjectSensedMongoRepository(
      inject[DB](identified by 'mainDb))

    bind[VirtualObjectSensedRepository] identifiedBy 'VirtualObjectSensedRepository to VirtualObjectSensedRepositoryImpl(
      inject[VirtualObjectSensedMongoRepository](identified by 'VirtualObjectSensedMongoRepository))

    bind[ListVirtualObjectSensed.UseCase] identifiedBy 'ListVirtualObjectSensedUseCase to ListVirtualObjectSensed
      .UseCase(inject[VirtualObjectSensedRepository](identified by 'VirtualObjectSensedRepository))

  }

  implicit val implicitCoreInjector = coreInjector

}
