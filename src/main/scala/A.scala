import akka.actor.{Actor, ActorLogging}

/**
 * Simple example of logging actor
 */
class A extends Actor with ActorLogging {
  def receive = {
    case msg => log.debug("received message: {}", msg)
  }
}
