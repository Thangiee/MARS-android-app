package com.uta.mars

import scala.language.implicitConversions

package object common extends AnyRef with Implicits {
  type Session = com.uta.mars.api.Session

  val Ok      = com.uta.mars.api.Ok
  val Err     = com.uta.mars.api.Err
  val MarsApi = com.uta.mars.api.MarsApi
}
