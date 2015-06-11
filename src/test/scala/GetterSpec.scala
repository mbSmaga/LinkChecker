import java.util.concurrent.Executor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}

import scala.concurrent.Future

/**
 * Created by mbsmaga on 09.06.15.
 */
class GetterSpec extends TestKit(ActorSystem("GetterSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  import GetterSpec._

  override def afterAll(): Unit = {
    system.terminate()
  }

  "A Getter" must {
    "return the right body" in {
      val getter = system.actorOf(Props(new StepParent(fakeGetter(firstLink, 2), testActor)), "rightBody")
      for (link <- links(firstLink))
        expectMsg(Controller.Check(link, 2))
      expectMsg(Getter.Done)
    }
    "properly finish in case of errors" in {
      val getter = system.actorOf(Props(new StepParent(fakeGetter("unknown", 3), testActor)), "wrongLink")
      expectMsg(Getter.Done)
    }
  }
}

class StepParent(child: Props, probe: ActorRef) extends Actor {
  context.actorOf(child, "probe")
  def receive = {
    case msg => probe.tell(msg, sender)
  }
}

object GetterSpec {
  val firstLink = "http://www.rkuhn.info/1"

  val bodies = Map(
    firstLink ->
      """
        |<html>
        | <head><title>Page 1</title></head>
        | <body>
        |   <h1>A Link</h1>
        |   <a href="http://www.rkhun.info/2">click here</a>
        | </body>
        |</html>
      """.stripMargin
  )

  val links = Map(firstLink -> Seq("http://www.rkhun.info/2"))

  object FakeWebClient extends WebClient {
    def get(url: String)(implicit executor: Executor): Future[String] = {
      bodies get url match {
        case None => Future.failed(BadStatus(404))
        case Some(body) => Future.successful(body)
      }
    }
  }

  def fakeGetter(url: String, depth: Int): Props = {
    Props(new Getter(url, depth) {
      override def client = FakeWebClient
    })
  }
}
