package com.uta.mars.api

case class Record(
  inTime: Long,
  inComputerId: Option[String],
  netId: String,
  id: Int,
  outTime: Option[Long],
  outComputerId: Option[String]
)

object Record {
  import play.api.libs.json._
  implicit val recordFmt = Json.reads[Record]
}

case class Records(records: Seq[Record])

object Records {
  import play.api.libs.json._
  implicit val RecordsFmt = Json.reads[Records]
}
