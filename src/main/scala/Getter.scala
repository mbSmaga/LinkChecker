import akka.actor.{Status, Actor}
import akka.pattern.pipe
import org.jsoup.Jsoup
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._

class Getter(url: String, depth: Int) extends Actor {

  import Getter._

  implicit val exec = context.dispatcher

  def findLinks(body: String): Iterator[String] = {
    val document = Jsoup.parse(body, url)
    val links = document.select("a[href]")
    for {
      link <- links.iterator.asScala
    } yield link.absUrl("href")
  }

  def client: WebClient = AsyncWebClient

  //this is same..
  client get url pipeTo self

  //..as this
  /*
  val future = WebClient.get(url)
  future.onComplete {
    case Success(body)  => self ! body
    case Failure(error) => self ! Status.Failure(error)
  }
  */

  def receive = {
    case body: String =>
      for (link <- findLinks(body))
        context.parent ! Controller.Check(link, depth)
      stop()
    case _: Status.Failure => stop()
    case Abort => stop()
  }

  def stop(): Unit = {
    context.parent ! Done
    context.stop(self)
  }

}

object Getter {
  object Done
  object Abort
}
