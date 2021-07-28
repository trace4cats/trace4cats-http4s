package io.janstenpickle.trace4cats.http4s.server

import cats.data.Kleisli
import cats.effect.IO
import io.janstenpickle.trace4cats.http4s.common.{RunIOToId, RunKleisliWithEmptyTraceContext, TraceContext}
import io.janstenpickle.trace4cats.http4s.server.syntax._
import io.janstenpickle.trace4cats.http4s.server.Instances._

class ResourceKleisliServerCtxSyntaxSpec
    extends BaseServerTracerSpec[IO, Kleisli[IO, TraceContext[IO], *]](
      RunIOToId,
      RunKleisliWithEmptyTraceContext,
      (routes, filter, ep) =>
        routes.tracedContext(
          Http4sResourceKleislis
            .fromHeadersContext(TraceContext.make[IO], requestFilter = filter)(ep.toKleisli)
        ),
      (app, filter, ep) =>
        app.tracedContext(
          Http4sResourceKleislis
            .fromHeadersContext(TraceContext.make[IO], requestFilter = filter)(ep.toKleisli)
        )
    )
