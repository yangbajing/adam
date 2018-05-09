package mesh.common.test

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{EitherValues, MustMatchers, OptionValues, WordSpecLike}

trait MeshSpec extends WordSpecLike with MustMatchers with OptionValues with EitherValues with ScalaFutures {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(Span(30, Seconds), Span(50, Milliseconds))

}
