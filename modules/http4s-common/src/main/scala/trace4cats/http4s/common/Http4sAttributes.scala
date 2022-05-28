package trace4cats.http4s.common

import cats.effect.SyncIO
import org.typelevel.vault.Key

object Http4sAttributes {
  object Keys {
    val SpanName: Key[String] = Key.newKey[SyncIO, String].unsafeRunSync()
  }
}
