package agent.dubbo.model

import java.util.concurrent.atomic.AtomicLong

case class Request(
  data: RpcInvocation,
  id: Long = Request.generateId,
  interfaceName: String = "com.alibaba.dubbo.performance.demo.provider.IHelloService",
  methodName: String = "hash",
  dubboVersion: String = "2.6.0",
  version: String = "0.0.0",
  parameterTypesString: String = "Ljava/lang/String;",
  towWay: Boolean = true,
  event: Boolean = false)

object Request {
  private val atomicLong = new AtomicLong()

  def generateId: Long = atomicLong.getAndIncrement()
}
