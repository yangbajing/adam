package agent

object Constants {
  val AKKA_SYSTEM = "MeshAgent"

  val TYPE = "type"

  val PROVIDER = "provider"
  val CONSUMER = "consumer"

  val MESH_ENDPOINT = "mesh-endpoint"

  object Service {
    val HELLO_SERVICE = "com.alibaba.dubbo.performance.demo.provider.IHelloService"
  }
}

object Configurations {
  import Constants._

  def runType: String = System.getProperty(TYPE)

  // TCP port
  def endpointPort: Int = System.getProperty("endpoint.port").toInt

  // HTTP port
  def serverPort: Int = System.getProperty("server.port").toInt

  def dubboPort: Int = System.getProperty("dubbo.protocol.port").toInt

}
