package io.janstenpickle.trace4cats.http4s.client

import io.janstenpickle.trace4cats.http4s.common.Response_
import io.janstenpickle.trace4cats.model.{AttributeValue, SemanticAttributeKeys}

object Http4sClientResponse {

  def toAttributes(res: Response_): Map[String, AttributeValue] = {
    Map[String, AttributeValue](
      SemanticAttributeKeys.httpStatusCode -> res.status.code,
    )
  }
}
