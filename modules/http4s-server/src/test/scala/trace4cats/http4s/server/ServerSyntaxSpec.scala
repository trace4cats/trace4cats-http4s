package trace4cats.http4s.server

import cats.data.Kleisli
import cats.effect.IO
import trace4cats.Span
import trace4cats.http4s.common.{RunIOToId, RunKleisliWithNoopSpan}
import trace4cats.http4s.server.syntax._

class ServerSyntaxSpec
    extends BaseServerTracerSpec[IO, Kleisli[IO, Span[IO], *]](
      RunIOToId,
      RunKleisliWithNoopSpan,
      (routes, filter, ep) => routes.inject(ep, requestFilter = filter),
      (app, filter, ep) => app.inject(ep, requestFilter = filter)
    )
