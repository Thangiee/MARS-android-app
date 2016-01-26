package com.uta.mars.app.common

import android.content.{DialogInterface, Context}
import android.os.Handler
import com.github.nscala_time.time.DurationBuilder
import com.github.nscala_time.time.Imports._
import com.typesafe.scalalogging.LazyLogging
import org.scaloid.common.AlertDialogBuilder

private[common] trait Base extends AnyRef with LazyLogging {
  private val handler = new Handler()
  private val repeatHandler = new Handler()
  protected implicit val session: SessionPref = SessionPref()

  def getCtx: Context

  def delay(duration: DurationBuilder)(f: => Unit): Unit = {
    handler.postDelayed(() => f, duration.millis)
  }

  def repeat(initDelay: DurationBuilder = 0.second, period: DurationBuilder = 1.second)(f: => Unit): Runnable = {
    lazy val runnable: Runnable = () => {f; repeatHandler.postDelayed(runnable, period.millis)}
    repeatHandler.postDelayed(runnable, initDelay.millis)
    runnable
  }

  def clearAllRepeatTasks(): Unit = repeatHandler.removeCallbacksAndMessages(null)

  def showApiErrorDialog(code: Int, btnName: String = "Dismiss", onClick: DialogInterface => Unit = d => d.dismiss()): Unit = {
    val (title, msg) = code match {
      case 400 => ("400: Bad Request", "The request contains bad syntax or cannot be fulfilled.")
      case 401 => ("401: Unauthorized", "Invalid authentication, check your credentials.")
      case 403 => ("403: Forbidden", "Invalid session, try logging in again.")
      case 404 => ("404: Not Found", "The requested resource that does not exist.")
      case 409 => ("409: Conflict", "The request could not be processed because of conflict in the request")
      case 410 => ("410: Gone", "The resource requested is no longer available")
      case 500 => ("500: Internal Server Error", "There was an internal server error. Please contact the administrator if this problem persist.")
      case 503 => ("503: Service Unavailable", "The server is currently unavailable (it's busy or down for maintenance). Try again later.")
      case 498 => ("No Internet Connection", "Check your connection and try again.")
      case _   => ("Unexpected Error", "The application has encounter an unexpected error.")
    }

    new AlertDialogBuilder(title, msg)(getCtx) {
      positiveButton(btnName, (d, _) => onClick(d))
    }.show()
  }
}
