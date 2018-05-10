package agent.dubbo.actors

import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

import agent.dubbo.DubboRpcCoder
import agent.dubbo.model.RpcRequest
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import akka.io.Tcp.{CommandFailed, ConnectionClosed, Received, Write}
import akka.io.{IO, Tcp}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.util.control.NonFatal

object DubboActor {

  val processingRpcs = new ConcurrentHashMap[String, (RpcRequest, Long, ActorRef)]()

  class WorkActor(conn: ActorRef, master: ActorRef) extends Actor with StrictLogging {

    import context.dispatcher

    override def receive: Receive = {
      case request: RpcRequest =>
        val from = sender()
        processingRpcs.put(String.valueOf(request.id), (request, 0, from))
        val bytes = DubboRpcCoder.encode(request)
        //        logger.debug(s"request: $request, processingRpc size: ${processingRpcs.size}\n$bytes")
        conn ! Write(bytes)

      case Received(bytes) =>
        //        logger.debug(s"Received data: $bytes")
        val response = DubboRpcCoder.decode(bytes)
        val maybe = processingRpcs.get(response.requestId)
        if (maybe ne null) {
          val (_, startNano, from) = maybe
          //        logger.info(s"[${response.requestId}] cost: ${java.time.Duration.ofNanos(System.nanoTime() - startNano)}")
          //        logger.debug(s"response: $response")
          processingRpcs.remove(response.requestId)
          from ! response
        }

      case w: Write =>
        logger.info(s"收到Write消息：$w")
        conn ! w

      case CommandFailed(w: Write) =>
        // O/S buffer was full
        logger.warn("write failed，1 second after retry")
        context.system.scheduler.scheduleOnce(1.second, self, w)

      case _: ConnectionClosed =>
        context.stop(master)
    }
  }

  def props(remoteAddress: InetSocketAddress) = Props(new DubboActor(remoteAddress))

  private def propsWork(conn: ActorRef, manager: ActorRef) = Props(new WorkActor(conn, manager))
}

/**
 * Dubbo actor由agent-provider使用
 * @param remoteAddress
 */
class DubboActor(remoteAddress: InetSocketAddress) extends Actor with StrictLogging {

  import DubboActor._
  import akka.io.Tcp._
  import context.system

  private var connection = Option.empty[ActorRef]
  private var pendingRequests = List.empty[RpcRequest]
  private var router: Router = _

  override def preStart(): Unit = {
    IO(Tcp) ! Connect(remoteAddress, options = List(SO.KeepAlive(true), SO.TcpNoDelay(true)))
  }

  override def postStop(): Unit = {
    connection.foreach { conn =>
      conn ! Close
      connection = None
    }
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    // TODO 明确需要监管的异常
    case NonFatal(e) =>
      logger.error("supervisorStrategy exception", e)
      SupervisorStrategy.defaultDecider(e)
  }

  override def receive: Receive = {
    case CommandFailed(_: Connect) =>
      logger.error("connect failed")
      context.stop(self)

    case c: Connected =>
      val conn = sender()
      conn ! Register(self)
      connection = Some(conn)
      router = createRouter(conn)
      context.become(receiveActive(conn))
      //      logger.debug(s"Dubbo RPC connected: $c，pendingRequests size: ${pendingRequests.size}")
      pendingRequests.reverse.foreach(request => router.route(Write(DubboRpcCoder.encode(request)), self))

    case request: RpcRequest => // 连接建立前缓存收到的发送请求
      val from = sender()
      processingRpcs.put(String.valueOf(request.id), (request, 0, from))
      //      logger.debug(s"pending request: $request, processingRpc size: ${processingRpcs.size}")
      pendingRequests ::= request
  }

  private def receiveActive(conn: ActorRef): Receive = {
    case request: RpcRequest =>
      router.route(request, sender())

    case received: Received =>
      router.route(received, sender())
  }

  private def createRouter(conn: ActorRef, routeeSize: Int = 8): Router = {
    val routees = Vector.fill(routeeSize) {
      val r = context.actorOf(propsWork(conn, self))
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

}
