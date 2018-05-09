package agent.dubbo.model

case class RpcInvocation(
    methodName: String,
    parameterTypes: String,
    arguments: Array[Byte],
    attachments: Map[String, String]) {
  override def toString: String = s"RpcInvocation($methodName, $parameterTypes, ${java.util.Arrays.toString(arguments)}, $attachments)"
}
