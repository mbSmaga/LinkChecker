import akka.actor.{Props, ActorRef, Actor}

class Receptionist extends Actor {

  import Receptionist._

  private val Depth = 2
  private var reqNo = 0

  def receive = waiting

  val waiting: Receive = {
    case Get(url) => context.become(runNext(Vector(Job(sender, url))))
  }

  private def runNext(queue: Vector[Job]): Receive = {
    reqNo += 1
    if (queue.isEmpty) waiting
    else {
      val controller = context.actorOf(Props[Controller], s"c$reqNo")
      controller ! Controller.Check(queue.head.url, Depth)
      running(queue)
    }
  }

  def running(queue: Vector[Job]): Receive = {
    case Controller.Result(links) =>
      val job = queue.head
      job.client ! Result(job.url, links)
      context.stop(sender)
      context.become(runNext(queue.tail))
    case Get(url) =>
      context.become(enqueueJob(queue, Job(sender, url)))
  }

  private def enqueueJob(queue: Vector[Job], job: Job): Receive = {
    if (queue.size > 3) {
      sender ! Failed(job.url)
      running(queue)
    } else running(queue :+ job)
  }
}

object Receptionist {

  case class Job(client: ActorRef, url: String)

  case class Get(url: String)

  case class Result(url: String, links: Set[String])

  case class Failed(url: String)

}
