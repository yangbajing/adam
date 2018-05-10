package agent

import agent.dubbo.model.{Request, RpcResponse}
import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern._
import akka.util.Timeout
import com.alibaba.fastjson.JSON
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.control.NonFatal

class HelloRouting(runType: String, meshActor: ActorRef)(implicit ec: ExecutionContext) extends StrictLogging {

  def invokeRoute: Route = (pathEndOrSingleSlash & post) {
    formFields(('interface, 'method, 'parameterTypesString, 'parameter)).as(Request) { rpcRequest =>
      runType match {
        case Constants.CONSUMER => consumer(rpcRequest)
        case _                  => complete((StatusCodes.InternalServerError, "Environment variable type is needed to set to provider or consumer."))
      }
    }
  }

  private implicit val timeout = Timeout(30.seconds)

  // agent-consumer -> agent-provider
  def consumer(rpcRequest: Request): Route = {
    val f = (meshActor ? rpcRequest)
      .map {
        case RpcResponse(requestId, bytes) =>
          try {
            Right(JSON.parseObject[Int](bytes, classOf[Integer]))
          } catch {
            case NonFatal(e) =>
              Left(s"parse messageId[$requestId] ${java.util.Arrays.toString(bytes)} to json error: ${e.toString}")
          }
        case _ =>
          Left("请求RpcResponse失败")
      }
    onSuccess(f) {
      case Right(ival) => complete(ival.toString)
      case Left(msg)   => complete((StatusCodes.InternalServerError, msg))
    }
  }

}
