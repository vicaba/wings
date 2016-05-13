package wings.actor.mqtt

import java.util.UUID

import database.mongodb.MongoEnvironment
import akka.actor._
import wings.actor.util.ActorUtilities
import wings.agent.{ArchitectureDriver, CoreAgent}
import wings.model.virtual.virtualobject.metadata.{VOMetadataIdentityManager, VOMetadata}

import wings.services.db.MongoEnvironment


object MqttActor {
  /**
   *
   * @param virtualObjectId the VirtualObject Id associated with this actor
   * @param broker the broker to connect to
   * @return a Props for creating this actor
   */
  def props(virtualObjectId: UUID, broker: String): Props = Props(new MqttActor(virtualObjectId, broker))
}

class MqttActor(val virtualObjectId: UUID, val broker: String)
  extends CoreAgent with ActorUtilities {

  override val toDeviceProps: Props = MqttDriver.props(virtualObjectId, broker, self)

  override val mongoEnvironment: MongoEnvironment = MongoEnvironment

  override val toArchitectureProps: Props = ArchitectureDriver.props(virtualObjectId, self)

}