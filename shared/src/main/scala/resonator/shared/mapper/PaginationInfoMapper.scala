package resonator.shared.mapper

import resonator.proto.api.feed.PaginationInfo
import resonator.shared.model

object PaginationInfoMapper {

  def fromProto(proto: PaginationInfo): model.PaginationInfo = {
    model.PaginationInfo(
      pageIndex = proto.pageIndex,
      pageSize = proto.pageSize
    )
  }

  def toProto(paginationInfo: model.PaginationInfo): PaginationInfo = {
    PaginationInfo(
      pageIndex = paginationInfo.pageIndex,
      pageSize = paginationInfo.pageSize
    )
  }

}
