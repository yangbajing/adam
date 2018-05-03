package agent.dubbo.model

case class RpcResponse(requestId: String, bytes: Array[Byte]) {
  override def toString: String = s"RpcResponse($requestId, ${java.util.Arrays.toString(bytes)})"
}
