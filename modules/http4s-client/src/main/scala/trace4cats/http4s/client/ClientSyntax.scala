package trace4cats.http4s.client

import cats.effect.kernel.MonadCancelThrow
import org.http4s.{Headers, Response}
import org.http4s.client.Client
import org.typelevel.ci.CIString
import trace4cats.context.Provide
import trace4cats.http4s.common.Http4sSpanNamer
import trace4cats.model.{AttributeValue, TraceHeaders}
import trace4cats.optics.{Getter, Lens}
import trace4cats.{Span, ToHeaders}

trait ClientSyntax {
  implicit class TracedClient[F[_]](client: Client[F]) {

    private[client] def liftTraceOld[G[_]](
      toHeaders: ToHeaders = ToHeaders.standard,
      spanNamer: Http4sSpanNamer = Http4sSpanNamer.methodWithPath
    )(implicit P: Provide[F, G, Span[F]], F: MonadCancelThrow[F], G: MonadCancelThrow[G]): Client[G] =
      ClientTracer
        .liftTrace[F, G, Span[F]](client, Lens.id, Getter((toHeaders.fromContext _).compose(_.context)), spanNamer)

    def liftTrace[G[_]](
      toHeaders: ToHeaders = ToHeaders.standard,
      spanNamer: Http4sSpanNamer = Http4sSpanNamer.methodWithPath,
      dropHeadersWhen: CIString => Boolean = Headers.SensitiveHeaders.contains,
      responseAttributesGetter: Getter[Response[F], Map[String, AttributeValue]] = Getter(_ => Map.empty)
    )(implicit P: Provide[F, G, Span[F]], F: MonadCancelThrow[F], G: MonadCancelThrow[G]): Client[G] =
      ClientTracer
        .liftTrace[F, G, Span[F]](
          client,
          Lens.id,
          Getter((toHeaders.fromContext _).compose(_.context)),
          spanNamer,
          dropHeadersWhen,
          responseAttributesGetter
        )

    private[client] def liftTraceContextOld[G[_], Ctx](
      spanLens: Lens[Ctx, Span[F]],
      headersGetter: Getter[Ctx, TraceHeaders],
      spanNamer: Http4sSpanNamer = Http4sSpanNamer.methodWithPath
    )(implicit P: Provide[F, G, Ctx], F: MonadCancelThrow[F], G: MonadCancelThrow[G]): Client[G] =
      ClientTracer
        .liftTrace[F, G, Ctx](client, spanLens, headersGetter, spanNamer)

    def liftTraceContext[G[_], Ctx](
      spanLens: Lens[Ctx, Span[F]],
      headersGetter: Getter[Ctx, TraceHeaders],
      spanNamer: Http4sSpanNamer = Http4sSpanNamer.methodWithPath,
      dropHeadersWhen: CIString => Boolean = Headers.SensitiveHeaders.contains,
      responseAttributesGetter: Getter[Response[F], Map[String, AttributeValue]] = Getter(_ => Map.empty)
    )(implicit P: Provide[F, G, Ctx], F: MonadCancelThrow[F], G: MonadCancelThrow[G]): Client[G] =
      ClientTracer
        .liftTrace[F, G, Ctx](client, spanLens, headersGetter, spanNamer, dropHeadersWhen, responseAttributesGetter)
  }
}
