package com.uta.mars

import java.util.concurrent.{TimeUnit, ScheduledThreadPoolExecutor}

import android.os.{Handler, Looper}

import scala.concurrent.duration._

package object common {

  private val handler = new Handler()
  private val repeatHandler = new Handler()

  def delay(mills: Long)(f: => Unit): Unit = {
    handler.postDelayed(() => f, mills)
  }

  def repeat(initDelay: Duration = 0.second, period: Duration = 1.second)(f: => Unit): Runnable = {
    lazy val runnable: Runnable = () => {f; repeatHandler.postDelayed(runnable, period.toMillis)}
    repeatHandler.postDelayed(runnable, initDelay.toMillis)
    runnable
  }

  def clearAllRepeatTasks(): Unit = repeatHandler.removeCallbacksAndMessages(null)
}
