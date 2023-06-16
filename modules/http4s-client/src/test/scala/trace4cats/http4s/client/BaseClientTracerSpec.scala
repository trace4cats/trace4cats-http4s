package trace4cats.http4s.client

import cats.data.NonEmptyList
import cats.effect.kernel.{Async, Ref}
import cats.implicits._
import cats.{~>, Eq, Eval, Id}
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.implicits._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import trace4cats.context.Provide
import trace4cats.http4s.common.{Http4sAttributes, Http4sHeaders, Http4sStatusMapping}
import trace4cats.kernel.{SpanCompleter, SpanSampler}
import trace4cats.model.TraceHeaders
import trace4cats._

import scala.annotation.nowarn

@nowarn("msg=.*unused value of type org.scalatest.Assertion.*")
abstract class BaseClientTracerSpec[F[_]: Async, G[_]: Async: Trace, Ctx](
  unsafeRunK: F ~> Id,
  makeSomeContext: Span[F] => Ctx,
  liftClient: Client[F] => Client[G]
)(implicit P: Provide[F, G, Ctx])
    extends AnyFlatSpec
    with ScalaCheckDrivenPropertyChecks
    with Matchers
    with Http4sClientDsl[G]
    with Http4sDsl[F] {

  implicit val responseArb: Arbitrary[Response[F]] =
    Arbitrary(
      Gen.oneOf(
        List(
          Ok(),
          BadRequest(),
          Unauthorized.apply(`WWW-Authenticate`.apply(NonEmptyList.one(Challenge("", "", Map.empty)))),
          Forbidden(),
          TooManyRequests(),
          BadGateway(),
          ServiceUnavailable(),
          GatewayTimeout()
        ).map(unsafeRunK.apply)
      )
    )

  it should "correctly set request headers and span status when the response body is read" in test(
    _.expect[String](_).void
  )

  it should "correctly set request headers and span status when the response body is not read" in test(_.status(_).void)

  def test(runReq: (Client[G], Request[G]) => G[Unit]): Assertion =
    forAll { (rootSpan: String, req1Span: String, req2Span: String, response: Response[F]) =>
      val rootSpanName = s"root: $rootSpan"
      val req1SpanName = s"req1: $req1Span"
      val req2SpanName = s"req2: $req2Span"

      val (httpApp, headersRef) = makeHttpApp(response)

      unsafeRunK(RefSpanCompleter[F]("test").flatMap { completer =>
        withClient(httpApp) { client =>
          def req(body: String): G[Unit] = {
            val req = GET(body, Uri.unsafeFromString(s"/")).withAttribute(
              Http4sAttributes.Keys.ExtraRequestAttributes,
              Eval.now(Map("test" -> AttributeValue.stringToTraceValue("works")))
            )
            runReq(client, req)
          }

          for {
            _ <- entryPoint(completer)
              .root(rootSpanName)
              .use { span =>
                P.provideK(makeSomeContext(span))(
                  Trace[G]
                    .span(req1SpanName)(req(req1SpanName))
                    .handleError(_ => ()) >> Trace[G].span(req2SpanName)(req(req2SpanName)).handleError(_ => ())
                )
              }
            spans <- completer.get
            headersMap <- headersRef.get
          } yield {
            (spans.toList.map(_.name) should contain)
              .theSameElementsAs(List("GET /", "GET /", rootSpanName, req1SpanName, req2SpanName))
            (headersMap.keys should contain).theSameElementsAs(Set(req1SpanName, req2SpanName))

            assert(
              Eq.eqv(
                ToHeaders.w3c.toContext(headersMap(req1SpanName)).get.spanId,
                spans.toList.sortBy(_.`end`.toEpochMilli).find(_.name == "GET /").get.context.spanId
              )
            )

            assert(
              Eq.eqv(
                ToHeaders.w3c.toContext(headersMap(req2SpanName)).get.spanId,
                spans.toList.sortBy(_.`end`.toEpochMilli).reverse.find(_.name == "GET /").get.context.spanId
              )
            )

            spans.head.attributes("test").toString() should be("works")

            val expectedStatus = Http4sStatusMapping.toSpanStatus(response.status)
            (spans.toList.collect {
              case span if span.name == "GET /" => span.status
            } should contain).theSameElementsAs(List.fill(2)(expectedStatus))

          }
        }
      })
    }

  def entryPoint(completer: SpanCompleter[F]): EntryPoint[F] = EntryPoint[F](SpanSampler.always[F], completer)

  def makeHttpApp(resp: Response[F]): (HttpApp[F], Ref[F, Map[String, TraceHeaders]]) = {
    val headersRef = Ref.unsafe[F, Map[String, TraceHeaders]](Map.empty)

    HttpRoutes
      .of[F] { case req @ GET -> Root =>
        req
          .as[String]
          .flatMap { key =>
            headersRef.update(_.updated(key, Http4sHeaders.converter.from(req.headers)))
          }
          .as(resp)
      }
      .orNotFound -> headersRef
  }

  def withClient(app: HttpApp[F])(fa: Client[G] => F[Assertion]): F[Assertion] =
    fa(liftClient(Client.fromHttpApp(app)))

}
