package trace4cats.http4s.server

import cats.data.Kleisli
import cats.effect.IO
import trace4cats.http4s.common.{RunIOToId, RunKleisliWithEmptyTraceContext, TraceContext}
import trace4cats.http4s.server.syntax._
import trace4cats.http4s.server.Instances._

class ServerCtxSyntaxSpec
    extends BaseServerTracerSpec[IO, Kleisli[IO, TraceContext[IO], *]](
      RunIOToId,
      RunKleisliWithEmptyTraceContext,
      (routes, filter, ep) => routes.injectContext(ep, makeContext = TraceContext.make[IO], requestFilter = filter),
      (app, filter, ep) => app.injectContext(ep, makeContext = TraceContext.make[IO], requestFilter = filter),
    )
