package agent.dubbo.actors

import java.net.InetSocketAddress

import agent.dubbo.DubboRpcCoder
import agent.dubbo.model.Request
import akka.actor.{ Actor, ActorRef, OneForOneStrategy, Props, SupervisorStrategy }
import akka.io.{ IO, Tcp }
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.control.NonFatal

object ClientActor {

  def props(remoteAddress: InetSocketAddress) = Props(new ClientActor(remoteAddress))

}

class ClientActor(remoteAddress: InetSocketAddress) extends Actor with StrictLogging {

  import akka.io.Tcp._
  import context.{ dispatcher, system }

  private val processingRpcs = mutable.Map.empty[String, (Request, ActorRef)]
  private var connection = Option.empty[ActorRef]
  private var pendingRequests = List.empty[Request]

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
      logger.info(s"Dubbo RPC connected: $c")
      conn ! Register(self)
      connection = Some(conn)
      context.become(active(conn))
      pendingRequests.reverse.foreach(request => conn ! Write(DubboRpcCoder.encode(request)))

    case request: Request => // 连接建立前缓存收到的发送请求
      val from = sender()
      processingRpcs.put(String.valueOf(request.id), (request, from))
      logger.debug(s"pending request: $request, processingRpc size: ${processingRpcs.size}")
      pendingRequests ::= request
  }

  private def active(conn: ActorRef): Receive = {
    case request: Request =>
      val from = sender()
      processingRpcs.put(String.valueOf(request.id), (request, from))
      val bytes = DubboRpcCoder.encode(request)
      logger.debug(s"request: $request, processingRpc size: ${processingRpcs.size}\n$bytes")
      conn ! Write(bytes)

    case Received(bytes) =>
      logger.debug(s"Received data: $bytes")
      val response = DubboRpcCoder.decode(bytes)
      val maybe = processingRpcs.get(response.requestId)
      logger.debug(s"response: $response, processingRpc: $maybe")
      maybe.foreach {
        case (_, from) =>
          processingRpcs -= response.requestId
          from ! response
      }

    case w: Write =>
      conn ! w

    case CommandFailed(w: Write) =>
      // O/S buffer was full
      logger.warn("write failed，1 second after retry")
      context.system.scheduler.scheduleOnce(1.second, self, w)

    case _: ConnectionClosed =>
      context.stop(self)
  }

}
