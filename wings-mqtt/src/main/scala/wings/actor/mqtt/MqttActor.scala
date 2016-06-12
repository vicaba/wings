package wings.actor.mqtt

import java.util.UUID

import database.mongodb.MongoEnvironment
import akka.actor._
import wings.actor.util.ActorUtilities
import wings.agent.{ArchitectureDriver, CoreAgent}
import wings.model.virtual.virtualobject.metadata.{VOMetadataIdentityManager, VOMetadata}

import wings.services.db.MongoEnvironment


object MqttActor {

  def props(virtualObjectId: UUID, conn: ActorRef): Props = Props(new MqttActor(virtualObjectId, conn))
}

case class MqttActor(virtualObjectId: UUID, conn: ActorRef)

  extends CoreAgent with ActorUtilities {

  override val toDeviceProps: Props = MqttDriver.props(virtualObjectId, conn, self)

  override val mongoEnvironment: MongoEnvironment = MongoEnvironment

  override val toArchitectureProps: Props = ArchitectureDriver.props(virtualObjectId, self)

}