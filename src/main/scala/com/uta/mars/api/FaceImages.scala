package com.uta.mars.api

import play.api.libs.json._

case class Image(id: String, url: String)

object Image {
  implicit val imageFmt = Json.reads[Image]
}

case class FaceImages(images: Seq[Image])

object FaceImages {
  implicit val faceImagesFmt = Json.reads[FaceImages]
}
