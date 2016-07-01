package bar.controllers

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import com.google.inject.Inject
import common.BarEvents
import common.BarEvents.EntityInserted
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class MainController @Inject()(system: ActorSystem)(implicit ec: ExecutionContext)
    extends Controller {

  val entities  = system.actorOf(Props(new EntitiesService))
  val random    = new Random()
  implicit val timeout = Timeout(5.seconds)

  def list() = Action.async {
    val currently = (entities ? "get").mapTo[Seq[Int]]
    currently.map(x => Ok(Json.toJson(x)))
  }

  def insert() = Action {
    val next = random.nextInt
    entities ! next
    Created(Json.toJson(random.nextInt))
  }
}

class EntitiesService extends Actor with ActorLogging {
  import akka.cluster.pubsub.DistributedPubSubMediator.Publish
  val mediator = DistributedPubSub(context.system).mediator
  val topic = BarEvents.KEY

  val existingEntities = ArrayBuffer[Int]()

  def receive = {
    case "get" => sender ! existingEntities
    case next: Int =>
      mediator ! Publish(topic, existingEntities)
      existingEntities.append(next)
      log.warning(s"publishing $existingEntities")
  }
}
