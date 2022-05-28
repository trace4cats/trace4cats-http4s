package trace4cats.http4s.common

import cats.data.Kleisli
import cats.effect.IO
import cats.~>

object RunKleisliWithEmptyTraceContext extends (Kleisli[IO, TraceContext[IO], *] ~> IO) {
  def apply[A](fa: Kleisli[IO, TraceContext[IO], A]): IO[A] = TraceContext.empty[IO].flatMap(fa.run)
}
