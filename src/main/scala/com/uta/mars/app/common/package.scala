package com.uta.mars.app

import com.github.nscala_time.time.DurationBuilder

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.implicitConversions

package object common extends AnyRef with Implicits {
  type Session = com.uta.mars.api.Session

  val Ok      = com.uta.mars.api.Ok
  val Err     = com.uta.mars.api.Err
  val MarsApi = com.uta.mars.api.MarsApi

  // convert nscala-time to scala.concurrent.duration when necessary
  implicit def concurrentFiniteDurationFrom(d: DurationBuilder): FiniteDuration =
    Duration( d.millis, scala.concurrent.duration.MILLISECONDS)
}
