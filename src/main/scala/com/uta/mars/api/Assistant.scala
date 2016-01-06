package com.uta.mars.api

case class Assistant(
  rate: Double,
  netId: String,
  email: String,
  job: String,
  department: String,
  lastName: String,
  firstName: String,
  employeeId: String,
  threshold: Double,
  title: String,
  titleCode: String
)

object Assistant {
  import play.api.libs.json._

  implicit val assistantFmt = Json.reads[Assistant]
}
