package trace4cats.http4s.server

import cats.data.Kleisli
import cats.effect.IO
import trace4cats.http4s.common.{CommonInstances, TraceContext}
import trace4cats.Trace

object Instances extends CommonInstances {
  implicit val traceContextTrace: Trace[Kleisli[IO, TraceContext[IO], *]] =
    Trace.kleisliInstance[IO].lens[TraceContext[IO]](_.span, (c, span) => c.copy(span = span))
}
