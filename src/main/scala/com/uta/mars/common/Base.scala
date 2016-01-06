package com.uta.mars.common

import android.content.Context
import android.os.Handler
import com.github.nscala_time.time.DurationBuilder
import com.github.nscala_time.time.Imports._
import org.scaloid.common.AlertDialogBuilder

private[common] trait Base {
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

  def showApiErrorDialog(code: Int, msg: String): Unit = {
    val title = code match {
      case 400 => "400: Bad Request"
      case 401 => "401: Unauthorized"
      case 403 => "403: Forbidden"
      case 404 => "404: Not Found"
      case 409 => "409: Conflict"
      case 410 => "410: Gone"
      case 500 => "500: Internal Server Error"
      case 503 => "503: Service Unavailable"
      case _   => "Unexpected Error"
    }

    new AlertDialogBuilder(title, msg)(getCtx) {
      positiveButton("Dismiss")
    }.show()
  }
}
