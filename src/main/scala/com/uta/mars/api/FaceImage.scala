package com.uta.mars.api

import play.api.libs.json._

case class FaceImage(id: String, url: String)

object FaceImage {
  implicit val imageFmt = Json.reads[FaceImage]
}
