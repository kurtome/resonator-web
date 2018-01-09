package kurtome.dote.server.controllers.mappers

import dote.proto.api.common.ResponseStatus
import kurtome.dote.server.util.SideEffectResult

object StatusMapper extends ((SideEffectResult[_]) => ResponseStatus) {
  override def apply(result: SideEffectResult[_]): ResponseStatus = {
    ResponseStatus(
      success = result.effectSuccess,
      errorMessage = result.effectErrorMessage
    )
  }
}
