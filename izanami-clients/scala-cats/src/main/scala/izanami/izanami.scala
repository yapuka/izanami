package izanami
import cats.Monad
import izanami.data._
import cats._
import cats.implicits._

abstract class FeatureClient[F[_]: Monad] {

  def features(pattern: String, context: String =  ""): F[Features]

  def isActive(key: String, context: String =  ""): F[Boolean]

  def featureOrElse[T](key: String, context: String =  "")(ok: => T)(ko: => T): F[T] = {
    isActive(key, context).map {
      case true => ok
      case false => ko
    }
  }

  def featureOrElseF[T](key: String, context: String =  "")(ok: => F[T])(ko: => F[T]): F[T] = {
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