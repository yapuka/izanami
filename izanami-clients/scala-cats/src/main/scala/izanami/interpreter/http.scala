package izanami.interpreter
import cats._
import cats.effect.{Effect, _}
import cats.implicits._
import izanami.{FeatureClient, IzanamiClient}
import izanami.data.{Feature, Features}
import org.http4s.{Request, Uri}
import org.http4s.client.Client
import play.api.libs.json._

import scala.concurrent.ExecutionContext.global

case class ClientConfig(baseUrl: Uri)

case class CallError(message: String) extends Throwable
case class ParseError(errors: Seq[(JsPath, Seq[JsonValidationError])]) extends Throwable

private object PagingResult {
  import play.api.libs.json._
  import play.api.libs.json.Reads._
  import play.api.libs.functional.syntax._

  implicit val reads: Reads[PagingResult] = (
    (__ \ "metadata" \ "page").read[Int] and
      (__ \ "metadata" \ "pageSize").read[Int] and
      (__ \ "metadata" \ "nbPages").read[Int] and
      (__ \ "metadata" \ "count").read[Int] and
      (__ \ "results").read[Seq[JsValue]].orElse(Reads.pure(Seq.empty))
    )(PagingResult.apply _)
}

private case class PagingResult(
                                 page: Int,
                                 pageSize: Int,
                                 nbPages: Int,
                                 count: Int,
                                 results: Seq[JsValue])

object HttpInterpreter {
  def apply[F[_]: Effect](config: ClientConfig, client: Client[F]): IzanamiClient[F] = new HttpInterpreter(config, client)
}

class HttpInterpreter[F[_]: Effect](config: ClientConfig, client: Client[F]) extends IzanamiClient[F] {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO] = IO.timer(global)

  import play.api.libs.json.{JsObject, JsValue}
  import org.http4s._
  import org.http4s.headers._
  import org.http4s.MediaType

  override def features(pattern: String,
                          context: JsObject): F[Features] = {
    val requestForPage = (page: Int) =>  Request[F](
      uri = config.baseUrl / "api" / "features" +? ("pattern", pattern) +? ("active", "true") +? ("page", page) +? ("pageSize", 5),
      headers = Headers(Header("Izanami-Client-Id", "xxxx"), Header("Izanami-Client-Secret", "xxxx"), Accept(MediaType.application.json))
    )

    fetchDatas[Feature](requestForPage, 1) map {Features.apply}
  }

  override def isActive(key:  String, context:  JsObject): F[Boolean] = ???

  private def isLastPage(pagingResult: PagingResult): Boolean = {
    pagingResult.results.isEmpty || pagingResult.page === pagingResult.nbPages
  }

  private def fetchDatas[T](requestForPage: Int => Request[F], page: Int)(implicit reads: Reads[Seq[T]]): F[Seq[T]] = {
    import org.http4s.play._
    val M = MonadError[F, Throwable]
    val request = requestForPage(page)
    println(s"Request : $request")
    for {
      response        <- client.expect[JsValue](request)
      _               =  println(s"Response : $response")
      featuresOrError =  response.validate[PagingResult].asEither
      pagingResult    <- M.fromEither(featuresOrError.leftMap(err => ParseError(err)))
      resultOrErrors  =  reads.reads(JsArray(pagingResult.results)).asEither.leftMap(err => ParseError(err))
      datas           <- M.fromEither(resultOrErrors)
      allDatas        <- if (isLastPage(pagingResult)) datas.pure[F]
                         else fetchDatas(requestForPage, page + 1).map(_ ++  datas)
    } yield allDatas
  }

}
