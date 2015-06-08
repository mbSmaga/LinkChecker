import akka.actor._
import scala.concurrent.duration._

class Controller extends Actor with ActorLogging {
  import Controller._

  var cache = Set.empty[String]
  var children = Set.empty[ActorRef]

  context.setReceiveTimeout(10 seconds)

  def receive = {
    case Check(url, depth) =>
      log.debug("{} checking {}", depth, url)
      if (!cache(url) && depth > 0)
        children += context.actorOf(Props(new Getter(url, depth - 1)))
      cache += url
    case Getter.Done =>
      children -= sender
      if (children.isEmpty) context.parent ! Result(cache)
    case ReceiveTimeout => children foreach (_ ! Getter.Abort)
  }
}
object Controller {
  case class Check(url: String, depth: Int)
  case class Result(cache: Set[String])
}
