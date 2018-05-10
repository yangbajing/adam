package agent.registry

import java.net.InetAddress
import java.text.MessageFormat
import java.util.concurrent.Executors

import agent.{Configurations, Constants}
import com.coreos.jetcd.data.ByteSequence
import com.coreos.jetcd.kv.GetResponse
import com.coreos.jetcd.options.{GetOption, PutOption}
import com.coreos.jetcd.{Client, KV, Lease}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

trait IRegistry {
  // 注册服务
  @throws[Exception]
  def register(serviceName: String, port: Int): Unit

  @throws[Exception]
  def find(serviceName: String): Vector[Endpoint]
}

class EtcdRegistry(val registryAddress: String) extends IRegistry {
  private val logger: Logger = LoggerFactory.getLogger(classOf[EtcdRegistry])
  // 该EtcdRegistry没有使用etcd的Watch机制来监听etcd的事件
  // 添加watch，在本地内存缓存地址列表，可减少网络调用的次数
  // 使用的是简单的随机负载均衡，如果provider性能不一致，随机策略会影响性能
  private val rootPath: String = "dubbomesh"
  private val client: Client = Client.builder.endpoints(registryAddress).build
  private var leaseId: Long = 0L
  private var lease: Lease = _
  private var kv: KV = _

  def init(): EtcdRegistry = {
    lease = client.getLeaseClient
    kv = client.getKVClient
    try {
      this.leaseId = lease.grant(30).get.getID
    } catch {
      case e: Exception =>
        logger.warn(e.toString, e)
    }
    keepAlive()
    if (Constants.PROVIDER == Configurations.runType) { // 如果是provider，去etcd注册服务
      try {
        register(Constants.Service.HELLO_SERVICE, Configurations.endpointPort)
      } catch {
        case e: Exception =>
          logger.error("Provider向Etcd注册失败", e)
      }
    }
    this
  }

  // 向ETCD中注册服务
  override def register(serviceName: String, port: Int): Unit = { // 服务注册的key为:    /dubbomesh/com.some.package.IHelloService/192.168.100.100:2000
    val strKey = s"/$rootPath/$serviceName/${InetAddress.getLocalHost.getHostAddress}:$port"
    val key = ByteSequence.fromString(strKey)
    val value = ByteSequence.fromString("") // 目前只需要创建这个key,对应的value暂不使用,先留空
    kv.put(key, value, PutOption.newBuilder.withLeaseId(leaseId).build).get
    logger.info(s"Register a new mesh service at: $strKey")
  }

  // 发送心跳到ETCD,表明该host是活着的
  private def keepAlive(): Unit =
    Executors.newSingleThreadExecutor.submit(new Runnable {
      override def run(): Unit = {
        try {
          val listener = lease.keepAlive(leaseId)
          listener.listen
          logger.info("KeepAlive lease:" + leaseId + "; Hex format:" + leaseId.toHexString)
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    })

  override def find(serviceName: String): Vector[Endpoint] = {
    val strKey: String = MessageFormat.format("/{0}/{1}", rootPath, serviceName)
    val key: ByteSequence = ByteSequence.fromString(strKey)
    val response: GetResponse = kv.get(key, GetOption.newBuilder.withPrefix(key).build).get
    response.getKvs.asScala.map { kv =>
      val s = kv.getKey.toStringUtf8
      val index = s.lastIndexOf("/")
      val endpointStr = s.substring(index + 1, s.length)
      val Array(host, port) = endpointStr.split(":")
      Endpoint(host, port.toInt)
    }.toVector
  }

}
