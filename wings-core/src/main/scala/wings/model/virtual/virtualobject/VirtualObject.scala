package wings.model.virtual.virtualobject

import java.time.ZonedDateTime
import java.util.UUID

import wings.model.virtual.virtualobject.actuate.ActuateCapability
import wings.model.virtual.virtualobject.sense.SenseCapability
import wings.model.{ActorReferenced, HasIdentity, HasVoId, IdentityManager}
import play.api.libs.json._
import wings.virtualobject.domain.VirtualObject

// JSON library

import play.api.libs.json.Reads._

// Custom validation helpers

import play.api.libs.functional.syntax._

// Combinator syntax