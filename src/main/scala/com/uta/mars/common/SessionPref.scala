package com.uta.mars.common

import java.net.HttpCookie

import com.pixplicity.easyprefs.library.Prefs
import scala.collection.JavaConversions._

import scala.util.Try

case class SessionPref() extends Session {

  override lazy val authnCookies: Seq[HttpCookie] = Try(HttpCookie.parse(Prefs.getString("authn_cookies", "")).toList).getOrElse(Nil)

  def saveAuthnCookies(httpCookies: Seq[HttpCookie]) = Prefs.putString("authn_cookies", httpCookies.mkString("; "))
}
