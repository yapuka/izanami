package izanami
import izanami.data._
import cats._
import cats.effect.{ConcurrentEffect, _}
import cats.implicits._
import izanami.Types.ThrowableMonadError
import izanami.interpreter.{ClientConfig, HttpInterpreter}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object Types {
  type ThrowableMonadError[F[_]] =  MonadError[F, Throwable]
}

object IzanamiBuilder {
  def apply[F[_]](config: ClientConfig)(implicit ec: ExecutionContext, C: ConcurrentEffect[F]): IzanamiBuilder[F] = new IzanamiBuilder(config)(ec, C)
}

class IzanamiBuilder[F[_]](config: ClientConfig)(implicit ec: ExecutionContext, C: ConcurrentEffect[F]) {

  import cats.effect._
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  def resource: Resource[F, IzanamiClient[F]] = {
    val clientResource: Resource[F, Client[F]] = BlazeClientBuilder[F](ec).resource
    clientResource.flatMap { c =>
      Resource.pure(HttpInterpreter[F](config, c))
    }
  }
}

trait IzanamiClient[F[_]] extends FeatureClient[F] with ConfigClient[F] with ExperimentClient[F]

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