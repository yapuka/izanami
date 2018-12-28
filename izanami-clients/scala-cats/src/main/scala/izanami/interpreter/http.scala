package izanami.interpreter
import cats._
import cats.effect.{Effect, _}
import cats.implicits._
import izanami.FeatureClient
import izanami.data.{Feature, Features}
import org.http4s.client._
import play.api.libs.json.{JsObject, JsPath, JsValue, JsonValidationError}
import org.http4s._
import org.http4s.play._

import scala.concurrent.ExecutionContext.global

case class ClientConfig(baseUrl: String)

case class ParseError(errors: Seq[(JsPath, Seq[JsonValidationError])]) extends Throwable

class HttpInterpreter[F[_]: Effect](config: ClientConfig, client: Client[F]) extends FeatureClient[F] {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  import Feature._

  override def features(pattern: String,
                          context: JsObject): F[Features] = {
    for {
      features <- fetchFeatures(pattern, context, 1)
    } yield {
      Features(features)
    }
  }

  override def isActive(key:  String, context:  JsObject): F[Boolean] = ???


  private def fetchFeatures(pattern: String,
                        context: JsObject, page: Int): F[Seq[Feature]] = {
    val M = MonadError[F, Throwable]
    for {
      response        <- client.expect[JsValue](Uri.uri(config.baseUrl) / "api" / "features")
      featuresOrError = response.validate[Seq[Feature]].asEither
      features        <- M.fromEither(featuresOrError.leftMap(err => ParseError(err)))
      allFeatures     <- if (features.isEmpty) features.pure[F] else fetchFeatures(pattern, context, page + 1).map(_ ++  features)
    } yield {
      allFeatures
    }
  }


}
