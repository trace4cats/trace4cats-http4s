package trace4cats.http4s.client

import cats.effect.kernel.MonadCancelThrow
import org.http4s.client.Client
import trace4cats.context.Provide
import trace4cats.http4s.common.Http4sSpanNamer
import trace4cats.model.TraceHeaders
import trace4cats.optics.{Getter, Lens}
import trace4cats.{Span, ToHeaders}

trait ClientSyntax {
  implicit class TracedClient[F[_]](client: Client[F]) {
    def liftTrace[G[_]](
      toHeaders: ToHeaders = ToHeaders.standard,
      spanNamer: Http4sSpanNamer = Http4sSpanNamer.methodWithPath
    )(implicit P: Provide[F, G, Span[F]], F: MonadCancelThrow[F], G: MonadCancelThrow[G]): Client[G] =
      ClientTracer
        .liftTrace[F, G, Span[F]](client, Lens.id, Getter((toHeaders.fromContext _).compose(_.context)), spanNamer)

    def liftTraceContext[G[_], Ctx](
      spanLens: Lens[Ctx, Span[F]],
      headersGetter: Getter[Ctx, TraceHeaders],
      spanNamer: Http4sSpanNamer = Http4sSpanNamer.methodWithPath
    )(implicit P: Provide[F, G, Ctx], F: MonadCancelThrow[F], G: MonadCancelThrow[G]): Client[G] =
      ClientTracer
        .liftTrace[F, G, Ctx](client, spanLens, headersGetter, spanNamer)
  }
}
