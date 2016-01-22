package com.uta.mars.api

case class RecognitionResult(confidence: Double, threshold: Double)

object RecognitionResult {
  import play.api.libs.json._

  implicit val resultFmt = Json.reads[RecognitionResult]
}