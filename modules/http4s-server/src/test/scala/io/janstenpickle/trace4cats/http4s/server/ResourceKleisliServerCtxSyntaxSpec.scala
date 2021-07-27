package io.janstenpickle.trace4cats.http4s.server

import cats.data.Kleisli
import cats.effect.IO
import cats.{Id, ~>}
import io.janstenpickle.trace4cats.http4s.common.{RunIOToId, TraceContext}
import io.janstenpickle.trace4cats.http4s.server.syntax.*
import io.janstenpickle.trace4cats.http4s.server.Instances.*

class ResourceKleisliServerCtxSyntaxSpec
    extends BaseServerTracerSpec[IO, Kleisli[IO, TraceContext[IO], *]](
      RunIOToId,
      new ~>[Kleisli[IO, TraceContext[IO], *], IO] {
        override def apply[A](fa: Kleisli[IO, TraceContext[IO], A]): IO[A] = TraceContext.empty[IO].flatMap(fa.run)
      },
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
