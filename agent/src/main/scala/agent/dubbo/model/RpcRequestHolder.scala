package agent.dubbo.model

import java.util.concurrent.ConcurrentHashMap

import scala.concurrent.Future

object RpcRequestHolder {
  private val processingRpc = new ConcurrentHashMap[String, Future[Object]]()

  def put(requestId: String, rpcFuture: Future[Object]): Unit = processingRpc.put(requestId, rpcFuture)

  def get(requestId: String): Future[Object] = processingRpc.get(requestId)

  def remove(requestId: String): Future[Object] = processingRpc.remove(requestId)

}
