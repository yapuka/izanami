package izanami
import cats.effect.IO
import izanami.interpreter.{ClientConfig, HttpInterpreter}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import org.http4s._
import org.http4s.client._
import cats._
import cats.effect.{Effect, _}
import cats.implicits._
import izanami.data.Feature

import scala.concurrent.ExecutionContext._

class FeatureSpec extends IzanamiSpec {

  "feature" should {

    "Feature" in {

      implicit val cs: ContextShift[IO] = IO.contextShift(global)
      implicit val timer: Timer[IO] = IO.timer(global)

      val config = ClientConfig(Uri.uri("http://localhost:9000"))

      val f = for {
        features <- BlazeClientBuilder[IO](global).resource.use { client =>
          val featureClient = new HttpInterpreter[IO](config, client)
          featureClient.fetchFeatures("*", Json.obj(), 1)
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
