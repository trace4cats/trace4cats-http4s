package io.janstenpickle.trace4cats.http4s.server

import cats.data.Kleisli
import cats.effect.IO
import cats.{Id, ~>}
import io.janstenpickle.trace4cats.Span
import io.janstenpickle.trace4cats.http4s.common.{RunIOToId, RunKleisliWithNoopSpan}
import io.janstenpickle.trace4cats.http4s.server.Instances.*
import io.janstenpickle.trace4cats.http4s.server.syntax.*

class ServerSyntaxSpec
    extends BaseServerTracerSpec[IO, Kleisli[IO, Span[IO], *]](
      RunIOToId,
      RunKleisliWithNoopSpan,
      (routes, filter, ep) => routes.inject(ep, requestFilter = filter),
      (app, filter, ep) => app.inject(ep, requestFilter = filter)
    )
