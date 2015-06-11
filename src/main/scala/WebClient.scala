import java.util.concurrent.Executor

import com.ning.http.client.AsyncHttpClient

import scala.concurrent.{Promise, Future}

trait WebClient {
  def get(url: String)(implicit executor: Executor): Future[String]
}

case class BadStatus(status: Int) extends RuntimeException

object AsyncWebClient extends WebClient {
  private val client = new AsyncHttpClient

  def get(url: String)(implicit executor: Executor): Future[String] = {
    val javaFuture = client.prepareGet(url).execute()
    val promise = Promise[String]()
    javaFuture.addListener(new Runnable {
      override def run(): Unit = {
        val response = javaFuture.get
        if (response.getStatusCode < 400) promise.success(response.getResponseBodyExcerpt(131072))
        else promise.failure(BadStatus(response.getStatusCode))
      }
    }, executor)
    promise.future
  }

  def shutdown(): Unit = client.close()
}
