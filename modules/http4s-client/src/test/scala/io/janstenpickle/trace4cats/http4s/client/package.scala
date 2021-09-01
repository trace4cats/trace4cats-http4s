package io.janstenpickle.trace4cats.http4s

import cats.{~>, Monad}
import cats.data.Kleisli
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

  implicit def kleisliIdProvide[F[_]: Monad, R]: Provide[Kleisli[F, R, *], Kleisli[F, R, *], R] =
    new Provide[Kleisli[F, R, *], Kleisli[F, R, *], R] {
      def F: Monad[Kleisli[F, R, *]] = implicitly
      def Low: Monad[Kleisli[F, R, *]] = implicitly

      def lift[A](fa: Kleisli[F, R, A]): Kleisli[F, R, A] = fa
      def ask[R1 >: R]: Kleisli[F, R, R1] = Kleisli.ask
      def local[A](fa: Kleisli[F, R, A])(f: R => R): Kleisli[F, R, A] = fa.local(f)
      def provide[A](fa: Kleisli[F, R, A])(r: R): Kleisli[F, R, A] = fa.local(_ => r)
    }
}
