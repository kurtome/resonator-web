package kurtome.dote.web.shared.mapper

import dote.proto.api.common.{ActionStatus => ProtoStatus}
import kurtome.dote.web.shared.util.result._

object StatusMapper {
  def toProto(result: ActionStatus): ProtoStatus = {
    ProtoStatus(
      success = result.isSuccess,
      statusCode = result.code.id,
      errorCause = result.cause.id
    )
  }

  def fromProto(proto: ProtoStatus): ActionStatus = {
    if (proto.success) {
      SuccessStatus
    } else {
      ErrorStatus(ErrorCauses(proto.errorCause), StatusCodes(proto.statusCode))
    }
  }
}

