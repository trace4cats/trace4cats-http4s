package io.janstenpickle.trace4cats.http4s.common

import cats.{~>, Id}
import cats.effect.IO
import io.janstenpickle.trace4cats.http4s.common.CommonInstances._

object RunIOToId extends IO ~> Id {
  def apply[A](fa: IO[A]): Id[A] = fa.unsafeRunSync()
}
