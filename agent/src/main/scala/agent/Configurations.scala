package agent

object Constants {
  val TYPE = "type"

  val PROVIDER = "provider"
  val CONSUMER = "consumer"

  object Service {
    val HELLO_SERVICE = "com.alibaba.dubbo.performance.demo.provider.IHelloService"
  }

}

object Configurations {
  import Constants._

  def runType: String = System.getProperty(TYPE)

  def serverPort: Int = System.getProperty("server.port").toInt

  def dubboPort: Int = System.getProperty("dubbo.protocol.port").toInt

}
