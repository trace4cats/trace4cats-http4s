package io.janstenpickle.trace4cats.http4s.client

import cats.effect.kernel.{MonadCancelThrow, Resource}
import io.janstenpickle.trace4cats.Span
import io.janstenpickle.trace4cats.base.context.Provide
import io.janstenpickle.trace4cats.base.optics.{Getter, Lens}
import io.janstenpickle.trace4cats.http4s.common.{Http4sHeaders, Http4sSpanNamer, Http4sStatusMapping, Request_}
import io.janstenpickle.trace4cats.model.{SampleDecision, SpanKind, TraceHeaders}
import org.http4s.Request
import org.http4s.client.{Client, UnexpectedStatus}

object ClientTracer {
  def liftTrace[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow, Ctx](
    client: Client[F],
    spanLens: Lens[Ctx, Span[F]],
    headersGetter: Getter[Ctx, TraceHeaders],
    spanNamer: Http4sSpanNamer
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

              for {
                // only extract request attributes if the span is sampled as the address matching can be quite expensive
                _ <-
                  if (childSpan.context.traceFlags.sampled == SampleDecision.Include)
                    Resource.eval(childSpan.putAll(Http4sClientRequest.toAttributes(request)))
                  else Resource.unit[F]
                runClient = client.run _ // work around for a typer bug in Scala 3.0.1
                res <- runClient(req.mapK(P.provideK(childCtx)))
                  .evalTap { resp =>
                    childSpan.setStatus(Http4sStatusMapping.toSpanStatus(resp.status))
                  }
              } yield res
            }
            .mapK(P.liftK)
            .map(_.mapK(P.liftK))
        }
    }
}
