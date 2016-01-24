package com.uta.mars.app

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.github.adnansm.timelytextview.TimelyView
import com.github.adnansm.timelytextview.model.NumberUtils
import com.github.nscala_time.time.Imports._
import com.uta.mars.R
import com.uta.mars.app.common._
import org.scaloid.common._

class StopWatchView(ctx: Context, attrs: AttributeSet) extends FrameLayout(ctx, attrs) with BaseViewGroup {
  layoutInflater.inflate(R.layout.stop_watch_view, this, true)

  // views for the digits
  private val sTv  = find[TimelyView](R.id.timely_s)
  private val ssTv = find[TimelyView](R.id.timely_ss)
  private val mTv  = find[TimelyView](R.id.timely_m)
  private val mmTv = find[TimelyView](R.id.timely_mm)
  private val hTv  = find[TimelyView](R.id.timely_h)
  private val hhTv = find[TimelyView](R.id.timely_hh)

  private var startTime       = 0L
  private var isPaused        = true

  override def onAttachedToWindow(): Unit = {
    super.onAttachedToWindow()
    List(sTv, ssTv, mTv, mmTv, hTv, hhTv).foreach(_.setControlPoints(NumberUtils.getControlPointsFor(0)))
  }

  def stop(): Unit = {
    if (!isPaused) {
      clearAllRepeatTasks()
      isPaused = true
      startTime = -1
    }
  }

  def start(): Unit = {
    if (isPaused) {
      if (startTime == -1) {
        setTime(0)
        startTime = System.currentTimeMillis()
      }

      if (startTime == 0) {
        startTime = System.currentTimeMillis()
      }

      isPaused = false
      repeat(initDelay = 1.second ,period = 1.second) {
        animateTick(System.currentTimeMillis() - startTime)
      }
    }
  }

  def setTime(millis: Long): Unit = {
    startTime = System.currentTimeMillis() - millis

    val s = millis / 1000
    sTv.animate((s % 10).toInt).setDuration(100).start()
    ssTv.animate((s/10 % 6).toInt).setDuration(100).start()
    mTv.animate((s/60 % 10).toInt).setDuration(100).start()
    mmTv.animate((s/600 % 6).toInt).setDuration(100).start()
    hTv.animate((s/3600 % 10).toInt).setDuration(100).start()
    hhTv.animate((s/36000 % 10).toInt).setDuration(100).start()
  }

  private def animateTick(millis: Long, duration: Long = 500): Unit = {
    val s = millis / 1000
    //                V
    // animate 00:00:00 per second
    sTv.animate(((s - 1) % 10).toInt, (s % 10).toInt).setDuration(duration).start()

    //               V
    // animate 00:00:00 per 10 seconds
    if ((s-1)/10 != s/10) ssTv.animate(((s/10 - 1) % 6).toInt, ((s/10) % 6).toInt).setDuration(duration).start()

    //             V
    // animate 00:00:00 per minute
    if ((s-1)/60 != s/60) mTv.animate(((s/60 - 1) % 10).toInt, ((s/60) % 10).toInt).setDuration(duration).start()

    //            V
    // animate 00:00:00 per 10 minutes
    if ((s-1)/600 != s/600) mmTv.animate(((s/600 - 1) % 6).toInt, ((s/600) % 6).toInt).setDuration(duration).start()

    //          V
    // animate 00:00:00 per hour
    if ((s-1)/3600 != s/3600) hTv.animate(((s/3600 - 1) % 10).toInt, ((s/3600) % 10).toInt).setDuration(duration).start()

    //         V
    // animate 00:00:00 per 10 hour
    if ((s-1)/36000 != s/36000) hhTv.animate(((s/36000 - 1) % 10).toInt, ((s/30600) % 10).toInt).setDuration(duration).start()
  }
}