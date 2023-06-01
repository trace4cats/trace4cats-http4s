package trace4cats.http4s.client

import cats.data.Kleisli
import cats.effect.IO
import trace4cats.Span
import trace4cats.http4s.client.syntax._
import trace4cats.http4s.common.RunIOToId

class ClientSyntaxSpec
    extends BaseClientTracerSpec[IO, Kleisli[IO, Span[IO], *], Span[IO]](RunIOToId, identity, _.liftTraceOld())
