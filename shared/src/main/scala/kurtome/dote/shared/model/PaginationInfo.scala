package kurtome.dote.shared.model

case class PaginationInfo(pageIndex: Int, pageSize: Int) {
  lazy val offset = pageIndex * pageSize
}

object PaginationInfo {
  def apply(pageSize: Int): PaginationInfo = {
    PaginationInfo(pageIndex = 0, pageSize = pageSize)
  }
}

