package agent

import agent.dubbo.model.Request
import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern._
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class HelloRouting(runType: String, meshActor: ActorRef) extends StrictLogging {

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
    onComplete(meshActor.?(rpcRequest).mapTo[Integer]) {
      case Success(ival) => complete(ival.toString)
      case Failure(_)    => complete(StatusCodes.InternalServerError)
    }
  }

}
