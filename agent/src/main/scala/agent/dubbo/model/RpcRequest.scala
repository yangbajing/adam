package agent.dubbo.model

import java.io.{ByteArrayOutputStream, OutputStreamWriter, PrintWriter}
import java.util.concurrent.atomic.AtomicLong

case class Request(interface: String, method: String, parameterTypesString: String, parameter: String)

case class RpcRequest(
    id: Long,
    data: RpcInvocation,
    interfaceName: String = "com.alibaba.dubbo.performance.demo.provider.IHelloService",
    methodName: String = "hash",
    dubboVersion: String = "2.6.0",
    version: String = "0.0.0",
    parameterTypesString: String = "Ljava/lang/String;",
    towWay: Boolean = true,
    event: Boolean = false)

object RpcRequest {
  private val atomicLong = new AtomicLong()

  def generateId: Long = atomicLong.getAndIncrement()

  def apply(request: Request): RpcRequest = {
    val out = new ByteArrayOutputStream
    val writer = new PrintWriter(new OutputStreamWriter(out))
    JsonUtils.writeObject(request.parameter, writer)

    val invocation = RpcInvocation(request.method, request.parameterTypesString, out.toByteArray, Map("path" -> request.interface))
    RpcRequest(RpcRequest.generateId, invocation)
  }

}
