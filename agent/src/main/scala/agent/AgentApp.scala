package agent

import java.net.InetSocketAddress

import agent.mesh.actors.MeshActor
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

object AgentApp extends App with StrictLogging {
  implicit val system = ActorSystem(Constants.AKKA_SYSTEM)
  implicit val mat = ActorMaterializer()
  import system.dispatcher

  val runType = System.getProperty("type")

  val meshActor = system.actorOf(MeshActor.props(InetSocketAddress.createUnresolved("0.0.0.0", Configurations.endpointPort)), Constants.MESH_ENDPOINT)

  val bindingFuture = Http().bindAndHandle(new HelloRouting(runType, meshActor).invokeRoute, "0.0.0.0", Configurations.serverPort)
  bindingFuture.foreach { binding =>
    logger.info(s"AgentApp started: $binding")
  }
  bindingFuture.failed.foreach { e =>
    system.terminate()
    System.exit(-1)
  }

}
