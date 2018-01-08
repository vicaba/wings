package wings.coap.adapter

import java.net.{SocketException, URL}
import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.eclipse.californium.core.coap.CoAP.{Code, ResponseCode}
import org.eclipse.californium.core.coap.Response
import org.eclipse.californium.core.network.Exchange
import org.eclipse.californium.core.server.resources.CoapExchange
import org.eclipse.californium.core.{CoapResource, CoapServer}
import wings.coap.CoapMaster

case class Router(routes: Map[String, ActorRef])

case class LeafCoapResource(identifier: String, actor: ActorRef) extends CoapResource(identifier)
{

  // CoapExchange is not serializable so this can only be run locally
  override def handleRequest(exchange: Exchange): Unit = actor ! new CoapExchange(exchange, this)

}

@throws[SocketException]
class CaliforniumServer(router: Router) extends CoapServer
{

  val resources: Iterable[LeafCoapResource] = router.routes.map
  {
    case (id, actor) => LeafCoapResource(id, actor)
  }

  add(resources.toSeq: _*)
}

object Main {

  def main(args: Array[String]): Unit =
  {

    val system: ActorSystem = ActorSystem()

    val coapMaster: ActorRef = system.actorOf(CoapMaster.props())

    val actor: ActorRef = system.actorOf(Props(new Actor {
      override def receive: Receive = {
        case m: CoapExchange => m.respond(ResponseCode.VALID)
      }
    }))

    val router = Router(
      Map(
      "thing" -> coapMaster
      )
    )
    val server: CaliforniumServer = new CaliforniumServer(router)

    server.start()
  }

}

