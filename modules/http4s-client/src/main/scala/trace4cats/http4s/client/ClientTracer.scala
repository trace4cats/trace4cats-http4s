package trace4cats.http4s.client

import cats.effect.kernel.{MonadCancelThrow, Resource}
import cats.syntax.flatMap._
import org.http4s.{Headers, Request, Response}
import org.http4s.client.{Client, UnexpectedStatus}
import org.typelevel.ci.CIString
import trace4cats.Span
import trace4cats.context.Provide
import trace4cats.http4s.common.{Http4sHeaders, Http4sSpanNamer, Http4sStatusMapping, Request_, Response_}
import trace4cats.model.{AttributeValue, SampleDecision, SpanKind, TraceHeaders}
import trace4cats.optics.{Getter, Lens}

object ClientTracer {
  private[client] def liftTrace[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow, Ctx](
    client: Client[F],
    spanLens: Lens[Ctx, Span[F]],
    headersGetter: Getter[Ctx, TraceHeaders],
    spanNamer: Http4sSpanNamer
  )(implicit P: Provide[F, G, Ctx]): Client[G] =
    liftTrace[F, G, Ctx](
      client,
      spanLens,
      headersGetter,
      spanNamer,
      Headers.SensitiveHeaders.contains(_),
      Getter[Response[F], Map[String, AttributeValue]](_ => Map.empty)
    )

  def liftTrace[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow, Ctx](
    client: Client[F],
    spanLens: Lens[Ctx, Span[F]],
    headersGetter: Getter[Ctx, TraceHeaders],
    spanNamer: Http4sSpanNamer,
    dropHeadersWhen: CIString => Boolean,
    responseAttributesGetter: Getter[Response[F], Map[String, AttributeValue]]
  )(implicit P: Provide[F, G, Ctx]): Client[G] =
    Client { (request: Request[G]) =>
      Resource
        .eval(P.ask[Ctx])
        .flatMap { parentCtx =>
          val parentSpan = spanLens.get(parentCtx)
          parentSpan
            .child(
              spanNamer(request),
              SpanKind.Client,
              { case UnexpectedStatus(status, _, _) =>
                Http4sStatusMapping.toSpanStatus(status)
              }
            )
            .flatMap { childSpan =>
              val childCtx = spanLens.set(childSpan)(parentCtx)
              val headers = headersGetter.get(childCtx)
              val req: Request[G] = request.transformHeaders(_ ++ Http4sHeaders.converter.to(headers))
              val reqHeaderAttrs = Http4sHeaders.requestFields(req, dropHeadersWhen)
              val isSampled = childSpan.context.traceFlags.sampled == SampleDecision.Include
              // only extract request attributes if the span is sampled as the host parsing is quite expensive
              val reqExtraAttrs = if (isSampled) Http4sClientRequest.toAttributes(request) else Map.empty
              for {
                _ <- Resource.eval(childSpan.putAll(reqHeaderAttrs ++ reqExtraAttrs: _*))
                runClient = client.run _ // work around for a typer bug in Scala 3.0.1
                res <- runClient(req.mapK(P.provideK(childCtx)))
                  .evalTap { resp =>
                    val respHeaderAttrs = Http4sHeaders.responseFields(resp, dropHeadersWhen)
                    val respExtraAttrs = if (isSampled) responseAttributesGetter.get(resp) else Map.empty
                    childSpan.setStatus(Http4sStatusMapping.toSpanStatus(resp.status)) >>
                      childSpan.putAll(respHeaderAttrs ++ respExtraAttrs: _*)
                  }
              } yield res
            }
            .mapK(P.liftK)
            .map(_.mapK(P.liftK))
        }
    }
}
