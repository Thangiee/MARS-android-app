package com.uta.mars.app.common

import java.net.HttpCookie

import com.pixplicity.easyprefs.library.Prefs

import scala.collection.JavaConversions._
import scala.util.Try

case class SessionPref() extends Session {

  def cookies: Seq[HttpCookie] = Try(HttpCookie.parse(Prefs.getString("authn_cookies", "")).toList).getOrElse(Nil)

  def saveCookies(httpCookies: Seq[HttpCookie]) = Prefs.putString("authn_cookies", httpCookies.mkString("; "))

  def removeCookies(): Unit = Prefs.remove("authn_cookies")
}
