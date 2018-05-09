package agent

import agent.dubbo.RpcClient
import agent.registry.EtcdRegistry
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.alibaba.fastjson.JSON
import com.typesafe.scalalogging.StrictLogging

import scala.util.{Failure, Success}

class HelloRouting(implicit system: ActorSystem, mat: ActorMaterializer) extends StrictLogging {

  private val registry = new EtcdRegistry(System.getProperty("etcd.url"))
  registry.init()
  private lazy val rpcClient = new RpcClient
  private lazy val endpoints = registry.find(Constants.Service.HELLO_SERVICE)

  def invokeRoute: Route = (pathEndOrSingleSlash & post) {
    formFields(('interface, 'method, 'parameterTypesString, 'parameter)) { (interfaceName, method, parameterTypesString, parameter) =>
      System.getProperty("type") match {
        case "consumer" => consumer(interfaceName, method, parameterTypesString, parameter)
        case "provider" => provider(interfaceName, method, parameterTypesString, parameter)
        case _          => complete((StatusCodes.InternalServerError, "Environment variable type is needed to set to provider or consumer."))
      }
    }
  }

  def provider(interface: String, method: String, parameterTypesString: String, parameter: String): Route = {
    onSuccess(rpcClient.invoke(interface, method, parameterTypesString, parameter)) { response =>
      complete(response.bytes)
    }
  }

  private var robbinIdx = 0

  def consumer(interface: String, method: String, parameterTypesString: String, parameter: String): Route = {
    import system.dispatcher
    //    require(endpoints.nonEmpty, "无有效的服务")

    // 简单的负载均衡，随机取一个
    robbinIdx += 1
    val endpoint = endpoints(math.abs(robbinIdx % endpoints.size))
    val url = "http://" + endpoint.host + ":" + endpoint.port
    val formData = FormData(("interface", interface), ("method", method), ("parameterTypesString", parameterTypesString), ("parameter", parameter))

    val req = HttpRequest(HttpMethods.POST, url, entity = formData.toEntity)
    logger.debug(s"Selected endpoint: $endpoint")

    val resp = Http()
      .singleRequest(req)
      .flatMap(response =>
        Unmarshal(response.entity)
          .to[Array[Byte]]
          .map[Integer] { data =>
            logger.debug(s"consumer received bytes: ${java.util.Arrays.toString(data)}, ${new String(data)}")
            JSON.parseObject[Int](data, classOf[Integer])
          })
    onComplete(resp) {
      case Success(ival) => complete(ival.toString)
      case Failure(e)    => complete(StatusCodes.InternalServerError)
    }
  }

}
