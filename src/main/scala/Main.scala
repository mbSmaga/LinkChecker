import akka.actor.{ReceiveTimeout, Props, Actor}
import scala.concurrent.duration._

class Main extends Actor {

  import Receptionist._

  val receptionist = context.actorOf(Props[Receptionist], "receptionis")

  receptionist ! Get("http://m89.eu/")

  context.setReceiveTimeout(10 seconds)

  def receive = {
    case Result(url, set) =>
      println(set.toVector.sorted.mkString(s"Results for '$url': \n", "\n", "\n"))
    case Failed(url) =>
      println(s"Failed to fetch '$url'\n")
    case ReceiveTimeout =>
      context.stop(self)
  }

  override def postStop(): Unit = {
    AsyncWebClient.shutdown()
  }
}
