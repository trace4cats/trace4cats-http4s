package trace4cats.http4s.common

import cats.effect.IO
import cats.{~>, Id}
import trace4cats.http4s.common.CommonInstances._

object RunIOToId extends (IO ~> Id) {
  def apply[A](fa: IO[A]): Id[A] = fa.unsafeRunSync()
}
