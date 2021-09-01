package io.janstenpickle.trace4cats.http4s.client

import cats.data.Kleisli
import cats.effect.IO
import io.janstenpickle.trace4cats.Span
import io.janstenpickle.trace4cats.http4s.client.syntax._
import io.janstenpickle.trace4cats.http4s.common.RunIOToId

class ClientSyntaxSpec
    extends BaseClientTracerSpec[IO, Kleisli[IO, Span[IO], *], IO, Span[IO]](RunIOToId, identity, _.liftTrace())

class ClientTraceSyntaxSpec
    extends BaseClientTracerSpec[Kleisli[IO, Span[IO], *], Kleisli[IO, Span[IO], *], IO, Span[IO]](
      Span.noop[IO].useKleisliK.andThen(RunIOToId),
      identity,
      _.trace[IO]()
    )
