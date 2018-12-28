package izanami
import izanami.data._
import cats._
import cats.implicits._
import izanami.Types.ThrowableMonadError
import play.api.libs.json.{JsObject, Json}

object Types {
  type ThrowableMonadError[F[_]] =  MonadError[F, Throwable]
}

abstract class FeatureClient[F[_]: ThrowableMonadError] {

  def features(pattern: String, context: JsObject =  Json.obj()): F[Features]

  def isActive(key: String, context: JsObject =  Json.obj()): F[Boolean]

  def featureOrElse[T](key: String, context: JsObject =  Json.obj())(ok: => T)(ko: => T): F[T] = {
    isActive(key, context).map {
      case true => ok
      case false => ko
    }
  }

  def featureOrElseF[T](key: String, context: JsObject =  Json.obj())(ok: => F[T])(ko: => F[T]): F[T] = {
    for {
      active <- isActive(key, context)
      res     <- if (active) ok else ko
    } yield res
  }
}

trait ConfigClient[F[_]] {

}

trait ExperimentClient[F[_]] {

}