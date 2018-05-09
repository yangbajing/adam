package agent.mesh

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

import agent.dubbo.model.{ByteUtils, Request, RpcResponse}
import akka.serialization.Serializer
import akka.util.ByteString

class MeshSerializers extends Serializer {
  override def identifier: Int = 12345678

  override def includeManifest: Boolean = true

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case req: Request =>
      val bos = new ByteArrayOutputStream()
      val bsInterface = ByteString(req.interface)
      val bsMethod = ByteString(req.method)
      val bsParameterTypesString = ByteString(req.parameterTypesString)
      val bsParameter = ByteString(req.parameter)
      val offsetMethod = bsInterface.size
      val offsetParameterTypesString = offsetMethod + bsMethod.size
      val offsetParameter = offsetParameterTypesString + bsParameterTypesString.size
      val header = Array.ofDim[Byte](16)
      ByteUtils.int2bytes(0, header, 0)
      ByteUtils.int2bytes(offsetMethod, header, 4)
      ByteUtils.int2bytes(offsetParameterTypesString, header, 8)
      ByteUtils.int2bytes(offsetParameter, header, 12)
      val bsHeader = ByteString(header)
      (bsHeader ++ bsInterface ++ bsMethod ++ bsParameterTypesString ++ bsParameter).toArray

    case resp: RpcResponse =>
      val data = Array.ofDim[Byte](8 + resp.bytes.length)
      ByteUtils.long2bytes(resp.requestId.toLong, data, 0)
      System.arraycopy(resp.bytes, 0, data, 8, resp.bytes.length)
      data

    case _ =>
      throw new IllegalArgumentException("只支持：Request, RpcResponse")
  }

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = manifest match {
    case None => throw new IllegalArgumentException("需要 manifest")
    case Some(cls) =>
      if (cls == classOf[Request]) {
        val headerLength = 16
        val offsetMethod = ByteUtils.bytes2int(bytes, 4)
        val offsetParameterTypesString = ByteUtils.bytes2int(bytes, 8)
        val offsetParameter = ByteUtils.bytes2int(bytes, 12)
        Request(
          new String(java.util.Arrays.copyOfRange(bytes, headerLength, headerLength + offsetMethod), StandardCharsets.UTF_8),
          new String(java.util.Arrays.copyOfRange(bytes, headerLength + offsetMethod, headerLength + offsetParameterTypesString), StandardCharsets.UTF_8),
          new String(java.util.Arrays.copyOfRange(bytes, headerLength + offsetParameterTypesString, headerLength + offsetParameter), StandardCharsets.UTF_8),
          new String(java.util.Arrays.copyOfRange(bytes, headerLength + offsetParameter, bytes.length), StandardCharsets.UTF_8)
        )

      } else if (cls == classOf[RpcResponse]) {
        RpcResponse(String.valueOf(ByteUtils.bytes2long(bytes, 0)), java.util.Arrays.copyOfRange(bytes, 8, bytes.length))
      } else
        throw new IllegalArgumentException("manifest 只支持：Request, RpcResponse")
  }

}
