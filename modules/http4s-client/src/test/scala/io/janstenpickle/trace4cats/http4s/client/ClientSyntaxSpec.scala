package io.janstenpickle.trace4cats.http4s.client

import cats.data.Kleisli
import cats.effect.IO
import cats.{~>, Id}
import io.janstenpickle.trace4cats.Span
import io.janstenpickle.trace4cats.http4s.client.syntax._
import io.janstenpickle.trace4cats.http4s.client.Instances._
import io.janstenpickle.trace4cats.http4s.common.RunIOToId

class ClientSyntaxSpec
    extends BaseClientTracerSpec[IO, Kleisli[IO, Span[IO], *], Span[IO]](RunIOToId, identity, _.liftTrace())
