package io.janstenpickle.trace4cats.http4s

import cats.{~>, Monad}
import cats.arrow.FunctionK
import io.janstenpickle.trace4cats.base.context._

// instances from https://github.com/trace4cats/trace4cats/pull/608

package object client {

  implicit def idUnlift[F[_]: Monad]: Unlift[F, F] = new Unlift[F, F] {
    def F: Monad[F] = implicitly
    def Low: Monad[F] = implicitly

    def askUnlift: F[F ~> F] = F.pure(FunctionK.id)
    def lift[A](fa: F[A]): F[A] = fa
  }

  implicit def idProvide[Low[_], F[_], R](implicit P: Provide[Low, F, R]): Provide[F, F, R] = new Provide[F, F, R] {
    def ask[R1 >: R]: F[R1] = P.ask
    def F: cats.Monad[F] = P.F
    def Low: cats.Monad[F] = P.F
    def lift[A](la: F[A]): F[A] = la
    def local[A](fa: F[A])(f: R => R): F[A] = P.local(fa)(f)
    def provide[A](fa: F[A])(r: R): F[A] = P.lift(P.provide(fa)(r))
  }
}
