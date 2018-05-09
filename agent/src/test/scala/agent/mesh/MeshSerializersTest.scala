package agent.mesh

import agent.dubbo.model.{Request, RpcResponse}
import org.scalatest.{MustMatchers, WordSpec}

class MeshSerializersTest extends WordSpec with MustMatchers {

  "MeshSerializers" must {
    val ms = new MeshSerializers

    "request" in {
      val req = Request("interface", "method", "parameterTypesString", "parameter")
      val bytes = ms.toBinary(req)
      val req2 = ms.fromBinary(bytes, Some(classOf[Request])).asInstanceOf[Request]
      req mustBe req2
    }

    "fromBinary" in {
      val resp = RpcResponse("34", Array(1.toByte, 2.toByte, 3.toByte, 4.toByte))
      val bytes = ms.toBinary(resp)
      val resp2 = ms.fromBinary(bytes, Some(classOf[RpcResponse])).asInstanceOf[RpcResponse]
      resp.requestId mustBe resp2.requestId
      java.util.Arrays.equals(resp.bytes, resp2.bytes) mustBe true
    }
  }

}
