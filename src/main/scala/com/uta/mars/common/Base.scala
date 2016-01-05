package com.uta.mars.common

import android.content.Context
import android.os.Handler
import com.github.nscala_time.time.DurationBuilder
import com.github.nscala_time.time.Imports._

private[common] trait Base {
  private val handler = new Handler()
  private val repeatHandler = new Handler()

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
}
