package agent.dubbo

import java.io.{ByteArrayOutputStream, OutputStreamWriter, PrintWriter}
import java.net.InetSocketAddress

import agent.Configurations
import agent.dubbo.actors.ClientActor
import agent.dubbo.model.{JsonUtils, Request, RpcInvocation, RpcResponse}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future
import scala.concurrent.duration._

class RpcClient(implicit system: ActorSystem) extends StrictLogging {

  private val remoteAddress = InetSocketAddress.createUnresolved("localhost", Configurations.dubboPort)
  private val client = system.actorOf(ClientActor.props(remoteAddress), "dubbo-client")
  implicit private val timeout = Timeout(30.seconds)

  def invoke(interface: String, method: String, parameterTypesString: String, parameter: String): Future[RpcResponse] = {
    val out = new ByteArrayOutputStream
    val writer = new PrintWriter(new OutputStreamWriter(out))
    JsonUtils.writeObject(parameter, writer)

    val invocation = RpcInvocation(method, parameterTypesString, out.toByteArray, Map("path" -> interface))
    val request = Request(Request.generateId, invocation, version = "2.0.0")

    client.?(request).mapTo[RpcResponse]
  }

}
