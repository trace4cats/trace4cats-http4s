package io.janstenpickle.trace4cats.http4s.common

import cats.data.Kleisli
import cats.effect.IO
import cats.~>
import io.janstenpickle.trace4cats.Span

object RunKleisliWithNoopSpan extends Kleisli[IO, Span[IO], *] ~> IO {
  def apply[A](fa: Kleisli[IO, Span[IO], A]): IO[A] = Span.noop[IO].use(fa.run)
}
