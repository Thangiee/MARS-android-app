package com.uta.mars.common

import java.net.HttpCookie

import com.pixplicity.easyprefs.library.Prefs
import scala.collection.JavaConversions._

import scala.util.Try

case class SessionPref() extends Session {

  def authnCookies: Seq[HttpCookie] = Try(HttpCookie.parse(Prefs.getString("authn_cookies", "")).toList).getOrElse(Nil)

  def saveSessionCookies(httpCookies: Seq[HttpCookie]) = Prefs.putString("authn_cookies", httpCookies.mkString("; "))

  def removeSessionCookies(): Unit = Prefs.remove("authn_cookies")
}
