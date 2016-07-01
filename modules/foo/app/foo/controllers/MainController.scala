package foo.controllers

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import com.google.inject.Inject
import common.BarEvents
import play.api.libs.json.Json
import play.api.mvc._
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class MainController @Inject()(system: ActorSystem)(implicit ec: ExecutionContext)
    extends Controller {

  val cache            = system.actorOf(Props(new Cache()))
  val subscriber       = system.actorOf(Props(new Subscriber(BarEvents.KEY, cache)))
  implicit val timeout = Timeout(5.seconds)

  def list() = Action.async {
    val currently = (cache ? "get").mapTo[Seq[Int]]
    currently.map(x => Json.toJson(x)).map(x => Ok(x))
  }
}

class Subscriber(topic: String, forwardTo: ActorRef) extends Actor with ActorLogging {
  import akka.cluster.pubsub.DistributedPubSubMediator._
  val mediator = DistributedPubSub(context.system).mediator
  mediator ! Subscribe(BarEvents.KEY, self)

  def receive = {
    case ack: SubscribeAck =>
      log.warning("subscribing {}", ack)
    case x =>
      log.warning("forwarding {}", x)
      forwardTo ! x
  }
}

class Cache extends Actor with ActorLogging {
  var cached = Seq.empty[Int]

  def receive: Receive = {
    case "get" =>
      log.warning("getting cache")
      sender ! cached

    case entities: Seq[Int] =>
      log.warning(s"setting cache to $entities")
      cached = entities
  }
}
