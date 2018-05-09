package agent.dubbo

import java.io.{ByteArrayOutputStream, OutputStreamWriter, PrintWriter}

import agent.dubbo.model._
import akka.util.ByteString

import scala.collection.JavaConverters._

object DubboRpcCoder {
  // header length. byte
  protected val HEADER_LENGTH = 16
  // magic header.
  protected val MAGIC = 0xdabb.toShort
  // message flag.
  protected val FLAG_REQUEST = 0x80.toByte
  protected val FLAG_TWOWAY = 0x40.toByte
  protected val FLAG_EVENT = 0x20.toByte

  def decode(data: ByteString): RpcResponse = {
    val requestIdBytes = data.slice(4, 12)
    val requestId = ByteUtils.bytes2long(requestIdBytes, 0)
    val subBytes = data.drop(HEADER_LENGTH + 1)
    RpcResponse(requestId.toString, subBytes.toArray)
  }

  def encode(req: RpcRequest): ByteString = {
    val header = Array.ofDim[Byte](HEADER_LENGTH)

    // set magic number.
    ByteUtils.short2bytes(MAGIC, header)

    // set request and serialization flag.
    header(2) = (FLAG_REQUEST | 6).toByte

    if (req.towWay) header(2) = (header(2) | FLAG_TWOWAY).toByte
    if (req.event) header(2) = (header(2) | FLAG_EVENT).toByte

    // set request id.
    ByteUtils.long2bytes(req.id, header, 4)

    val bos = encodeRequestData(req.data)

    ByteUtils.int2bytes(bos.size, header, 12)

    // write header     ++ encode request data
    ByteString(header) ++ ByteString(bos.toByteArray)
  }

  private def encodeRequestData(inv: RpcInvocation): ByteArrayOutputStream = {
    val bos = new ByteArrayOutputStream
    val writer = new PrintWriter(new OutputStreamWriter(bos))
    JsonUtils.writeObject(inv.attachments.getOrElse("dubbo", "2.6.0"), writer)
    JsonUtils.writeObject(inv.attachments.get("path").orNull, writer)
    JsonUtils.writeObject(inv.attachments.getOrElse("version", "0.0.0"), writer)
    JsonUtils.writeObject(inv.methodName, writer)
    JsonUtils.writeObject(inv.parameterTypes, writer)
    JsonUtils.writeBytes(inv.arguments, writer)
    JsonUtils.writeObject(inv.attachments.asJava, writer)
    bos
  }

}
