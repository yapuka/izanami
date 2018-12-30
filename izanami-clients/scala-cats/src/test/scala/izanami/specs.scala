package izanami
import org.scalatest.{BeforeAndAfterAll, MustMatchers, OptionValues, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures, Waiters}

trait IzanamiSpec
  extends WordSpec
    with MustMatchers
    with OptionValues
    with ScalaFutures
    with Waiters
    with IntegrationPatience
    with BeforeAndAfterAll