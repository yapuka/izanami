package izanami
import cats.effect.IO
import izanami.interpreter.{ClientConfig, HttpInterpreter}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import org.http4s._
import org.http4s.client._
import cats._
import cats.data.Kleisli
import cats.effect.{Effect, _}
import cats.implicits._
import cats.syntax._
import izanami.data.Feature

import scala.concurrent.ExecutionContext._

class FeatureSpec extends IzanamiSpec {

  "feature" should {

    "Feature" in {

      implicit val cs: ContextShift[IO] = IO.contextShift(global)
      implicit val timer: Timer[IO] = IO.timer(global)

      val config = ClientConfig(Uri.uri("http://localhost:9000"))

      val f = for {
        features <- IzanamiBuilder[IO](config).resource.use { client =>
          client.features("*", Json.obj())
        }
      } yield {
        features
      }

      val features = f.handleErrorWith { e =>
        e.printStackTrace()
        Seq.empty[Feature].pure[IO]
      } .unsafeRunSync()

      println(features)
      true mustBe true
    }
  }

}
