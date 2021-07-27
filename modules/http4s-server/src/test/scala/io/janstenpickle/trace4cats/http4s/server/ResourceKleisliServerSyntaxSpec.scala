package io.janstenpickle.trace4cats.http4s.server

import cats.data.Kleisli
import cats.effect.IO
import cats.{Id, ~>}
import io.janstenpickle.trace4cats.Span
import io.janstenpickle.trace4cats.http4s.common.{RunIOToId, RunKleisliWithNoopSpan}
import io.janstenpickle.trace4cats.http4s.server.Instances.*
import io.janstenpickle.trace4cats.http4s.server.syntax.*

class ResourceKleisliServerSyntaxSpec
    extends BaseServerTracerSpec[IO, Kleisli[IO, Span[IO], *]](
      RunIOToId,
      RunKleisliWithNoopSpan,
      (routes, filter, ep) => routes.traced(Http4sResourceKleislis.fromHeaders(requestFilter = filter)(ep.toKleisli)),
      (app, filter, ep) => app.traced(Http4sResourceKleislis.fromHeaders(requestFilter = filter)(ep.toKleisli))
    )
