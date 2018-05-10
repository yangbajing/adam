package agent.mesh.actors

import java.net.InetSocketAddress

import agent.dubbo.actors.DubboActor
import agent.dubbo.model.{Request, RpcRequest}
import agent.registry.{Endpoint, EtcdRegistry}
import agent.{Configurations, Constants}
import akka.actor.{Actor, ActorRef, Props}
import akka.routing.{ActorSelectionRoutee, RoundRobinRoutingLogic, Router}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object MeshActor {

  def props(serverAddress: InetSocketAddress) = Props(new MeshActor(serverAddress))

}

class MeshActor(serverAddress: InetSocketAddress) extends Actor with StrictLogging {

  import context.dispatcher

  private implicit val timeout = Timeout(60.seconds)
  private val registry = new EtcdRegistry(System.getProperty("etcd.url")).init()
  private var dubboClient: ActorRef = _
  private var providerRouter: Router = _

  override def preStart(): Unit = {
    Configurations.runType match {
      case Constants.CONSUMER =>
        providerRouter = createProviderRouter(registry.find(Constants.Service.HELLO_SERVICE))
      case Constants.PROVIDER =>
        dubboClient = context.actorOf(DubboActor.props(InetSocketAddress.createUnresolved("localhost", Configurations.dubboPort)), "dubbo-client")
        context.become(receiveProvider)
      case unknown =>
        logger.error(s"未知运行状态：$unknown")
    }
  }

  override def postStop(): Unit = {
  }

  override def receive: Receive = {
    case request: Request => providerRouter.route(request, sender())
  }

  private def receiveProvider: Receive = {
    case request: Request => dubboClient.forward(RpcRequest(request))
  }

  private def createProviderRouter(endpoints: Vector[Endpoint]): Router = {
    //    logger.info(s"endpoints: $endpoints")
    val routees = endpoints.map { endpoint =>
      val r = context.actorSelection(s"akka://${Constants.AKKA_SYSTEM}@${endpoint.host}:${endpoint.port}/user/${Constants.MESH_ENDPOINT}")
      r.resolveOne().onComplete {
        case Success(ref) => context.watch(ref)
        case Failure(e)   => e.printStackTrace()
      }
      ActorSelectionRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

}
