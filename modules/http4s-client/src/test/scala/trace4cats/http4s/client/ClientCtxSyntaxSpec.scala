package trace4cats.http4s.client

import cats.data.Kleisli
import cats.effect.IO
import org.typelevel.ci.CIStringSyntax
import trace4cats.ToHeaders
import trace4cats.http4s.client.syntax._
import trace4cats.http4s.client.Instances._
import trace4cats.http4s.common._
import trace4cats.optics.Getter

class ClientCtxSyntaxSpec
    extends BaseClientTracerSpec[IO, Kleisli[IO, TraceContext[IO], *], TraceContext[IO]](
      RunIOToId,
      TraceContext("3d86cad5-d321-448f-a758-d28714fc1045", _),
      _.liftTraceContext(
        spanLens = TraceContext.span[IO],
        headersGetter = TraceContext.headers[IO](ToHeaders.all),
        Http4sSpanNamer.methodWithPath,
        Set(ci"foo.bar").contains,
        Getter(_ => Map(("test.attribute", "foo"))
        )
      )
    ){}
