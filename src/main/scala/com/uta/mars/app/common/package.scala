package com.uta.mars.app

import java.util.UUID

import com.github.nscala_time.time.DurationBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.implicitConversions

package object common extends AnyRef with Implicits {
  type Session = com.uta.mars.api.Session

  val Err     = com.uta.mars.api.Err
  val MarsApi = com.uta.mars.api.MarsApi
  val Good    = org.scalactic.Good
  val Bad     = org.scalactic.Bad

  // constants
  val UUID_KEY      = UUID.randomUUID().toString
  val COMP_ID_KEY   = UUID.randomUUID().toString
  val FACE_IMG_KEY  = UUID.randomUUID().toString

  // convert nscala-time to scala.concurrent.duration when necessary
  implicit def concurrentFiniteDurationFrom(d: DurationBuilder): FiniteDuration =
    Duration( d.millis, scala.concurrent.duration.MILLISECONDS)
}
