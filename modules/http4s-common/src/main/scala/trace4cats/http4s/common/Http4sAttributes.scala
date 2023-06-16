package trace4cats.http4s.common

import cats.Eval
import cats.effect.SyncIO
import org.typelevel.vault.Key
import trace4cats.model.AttributeValue

object Http4sAttributes {
  object Keys {
    val SpanName: Key[String] = Key.newKey[SyncIO, String].unsafeRunSync()
    val ExtraRequestAttributes: Key[Eval[Map[String, AttributeValue]]] =
      Key.newKey[SyncIO, Eval[Map[String, AttributeValue]]].unsafeRunSync()
    val ExtraResponseAttributes: Key[Eval[Map[String, AttributeValue]]] =
      Key.newKey[SyncIO, Eval[Map[String, AttributeValue]]].unsafeRunSync()
  }
}
