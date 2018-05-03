package agent

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

object AgentApp extends App with StrictLogging {
  implicit val system = ActorSystem("AgentApp")
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  val routing = new HelloRouting
  val bindingFuture = Http().bindAndHandle(routing.invokeRoute, "localhost", System.getProperty("server.port").toInt)
  bindingFuture.foreach { binding =>
    logger.info(s"AgentApp started: $binding")
  }
  bindingFuture.failed.foreach { e =>
    system.terminate()
    System.exit(-1)
  }

}
