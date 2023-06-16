package trace4cats.http4s.server

import cats.data.{Kleisli, OptionT}
import cats.effect.kernel.MonadCancelThrow
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.{FlatMap, Monad}
import org.http4s.{HttpApp, HttpRoutes, Request, Response}
import org.typelevel.ci.CIString
import trace4cats.context.Provide
import trace4cats.http4s.common.{Http4sAttributes, Http4sHeaders, Http4sStatusMapping, Request_, Response_}
import trace4cats.{ResourceKleisli, Trace}

object ServerTracer {
  def injectRoutes[F[_], G[_]: Monad: Trace, Ctx](
    routes: HttpRoutes[G],
    k: ResourceKleisli[F, Request_, Ctx],
    dropHeadersWhen: CIString => Boolean,
  )(implicit P: Provide[F, G, Ctx], F: MonadCancelThrow[F]): HttpRoutes[F] =
    Kleisli[OptionT[F, *], Request[F], Response[F]] { req =>
      val fa =
        for {
          ctx <- P.ask[Ctx]
          _ <- Trace[G].putAll(Http4sHeaders.requestFields(req, dropHeadersWhen): _*)
          _ <- Trace[G].putAll(
            req.attributes.lookup(Http4sAttributes.Keys.ExtraRequestAttributes).map(_.value).orEmpty.toSeq: _*
          )
          resp <-
            routes
              .run(req.mapK(P.liftK))
              .map(_.mapK(P.provideK(ctx)))
              .semiflatTap { res =>
                Trace[G].setStatus(Http4sStatusMapping.toSpanStatus(res.status)) *>
                  Trace[G].putAll(Http4sHeaders.responseFields(res, dropHeadersWhen): _*) *>
                  Trace[G].putAll(
                    res.attributes.lookup(Http4sAttributes.Keys.ExtraResponseAttributes).map(_.value).orEmpty.toSeq: _*
                  )
              }
              .value
        } yield resp

      OptionT[F, Response[F]] {
        k(req).use(P.provide(fa))
      }
    }

  def injectApp[F[_], G[_]: FlatMap: Trace, Ctx](
    app: HttpApp[G],
    k: ResourceKleisli[F, Request_, Ctx],
    dropHeadersWhen: CIString => Boolean,
  )(implicit P: Provide[F, G, Ctx], F: MonadCancelThrow[F]): HttpApp[F] =
    Kleisli[F, Request[F], Response[F]] { req =>
      val fa =
        for {
          ctx <- P.ask[Ctx]
          _ <- Trace[G].putAll(Http4sHeaders.requestFields(req, dropHeadersWhen): _*)
          _ <- Trace[G].putAll(
            req.attributes.lookup(Http4sAttributes.Keys.ExtraRequestAttributes).map(_.value).orEmpty.toSeq: _*
          )
          resp <- app.run(req.mapK(P.liftK)).map(_.mapK(P.provideK(ctx)))
          _ <- Trace[G].setStatus(Http4sStatusMapping.toSpanStatus(resp.status))
          _ <- Trace[G].putAll(Http4sHeaders.responseFields(resp, dropHeadersWhen): _*)
          _ <- Trace[G].putAll(
            resp.attributes.lookup(Http4sAttributes.Keys.ExtraResponseAttributes).map(_.value).orEmpty.toSeq: _*
          )
        } yield resp

      k(req).use(P.provide(fa))
    }
}
