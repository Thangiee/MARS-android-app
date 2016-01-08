package com.uta.mars.app

import android.content.Context
import android.graphics.{Canvas, Color, Paint}
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View.MeasureSpec
import com.github.adnansm.timelytextview.TimelyView
import com.github.adnansm.timelytextview.model.NumberUtils
import com.github.nscala_time.time.Imports._
import com.pnikosis.materialishprogress.ProgressWheel
import com.uta.mars.R
import com.uta.mars.app.common._
import org.scaloid.common._

class StopWatchView(ctx: Context, attrs: AttributeSet) extends CardView(ctx, attrs) with BaseViewGroup {

  // views for the digits
  private val sTv  = new TimelyView(ctx)
  private val ssTv = new TimelyView(ctx)
  private val mTv  = new TimelyView(ctx)
  private val mmTv = new TimelyView(ctx)
  private val hTv  = new TimelyView(ctx)

  private val progressWheel = new ProgressWheel(ctx)
  private val mPaint        = new Paint

  private val Radius = (R.styleable.StopWatchView, R.styleable.StopWatchView_swv_radius).r2PxSize(attrs)

  private var accumulatedTime = 0.millis.toDuration
  private var startTime       = 0.millis.toDuration
  private var _maxTime        = (9.hours + 59.minutes + 59.seconds).toDuration
  private var isPaused        = true

  override def onAttachedToWindow(): Unit = {
    super.onAttachedToWindow()
    setRadius(Radius)

    mPaint.setAntiAlias(true)
    mPaint.setColor(Color.BLACK)
    mPaint.setStrokeWidth(8.0f)
    mPaint.setStyle(Paint.Style.FILL)

    progressWheel.setLinearProgress(true)
    progressWheel.setCircleRadius((Radius * .99).toInt)
    progressWheel.setBarWidth(10)
    progressWheel.setBarColor(Color.HSVToColor(Array(120f, 1f, 1f))) // green
    progressWheel.setProgress(1)
    addView(progressWheel)

    List(sTv, ssTv, mTv, mmTv, hTv).foreach { tv =>
      tv.setControlPoints(NumberUtils.getControlPointsFor(0))
      addView(tv)
    }
  }

  override def onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int): Unit = {
    val w = getMeasuredWidth
    val h = getMeasuredHeight

    // all digits should have same w and h
    val digitW = sTv.getMeasuredWidth
    val digitH = sTv.getMeasuredHeight

    // top padding to center the digits vertically
    val topPadding = (h - digitH) / 2

    // can't think of a name :\
    // just a padding value to bring the digits closer to each other.
    val d = (digitW * .2083).toInt

    // padding value for the colon ":" btw the digits
    val c = (w * .05).toInt

    // position the second digits
    sTv.layout(w - digitW - d*2, topPadding, w - d*2, topPadding + digitH)
    ssTv.layout(w - (digitW*2) - d, topPadding, w - digitW - d, topPadding + digitH)

    // position the minute digits
    mTv.layout(w - (digitW*3) - c, topPadding, w - (digitW*2) - c, topPadding + digitH)
    mmTv.layout(w - (digitW*4) - c + d, topPadding, w - (digitW*3) - c + d, topPadding + digitH)

    // position the hour digit
    hTv.layout(0 + (d*2.5).toInt, topPadding, digitW + (d*2.5).toInt, topPadding + digitH)

    progressWheel.layout(0, 0, w, h)
  }

  override def dispatchDraw(canvas: Canvas): Unit = {
    super.dispatchDraw(canvas)
    val w = getMeasuredWidth
    val h = getMeasuredHeight

    val paddingH = (w * .025).toInt
    val paddingW = (sTv.getMeasuredWidth * .2083).toInt

    //          V
    // draw 0:00:00
    canvas.drawCircle(w - (w * .40).toInt, h/2 - paddingH, 6, mPaint)
    canvas.drawCircle(w - (w * .40).toInt, h/2 + paddingH, 6, mPaint)

    //       V
    // draw 0:00:00
    canvas.drawCircle(w - (w * .76).toInt + paddingW, h/2 - paddingH, 6, mPaint)
    canvas.drawCircle(w - (w * .76).toInt + paddingW, h/2 + paddingH, 6, mPaint)
  }

  override def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): Unit = {
    val wSpec = MeasureSpec.makeMeasureSpec((Radius * 2 * .18).toInt, MeasureSpec.AT_MOST)
    val hSpec = MeasureSpec.makeMeasureSpec((Radius * 2 * .20).toInt, MeasureSpec.AT_MOST)
    measureChildren(wSpec, hSpec)

    setMeasuredDimension(Radius * 2, Radius * 2)
  }

  /**
   * Get the elapsed time that is accurate to the nearest thousandth of a second.
   * Elapsed time does not include the time while the stop watch is stopped.
   *
   * @return the elapsed time in milliseconds */
  def elapsedTime: Long =
    if (isPaused) accumulatedTime.millis else (accumulatedTime + System.currentTimeMillis() - startTime).getMillis

  /** Stop the stop watch */
  def stop(): Unit = {
    if (!isPaused) {
      clearAllRepeatTasks()
      accumulatedTime = accumulatedTime + System.currentTimeMillis() - startTime
      isPaused = true
    }
  }

  /** start the stop watch */
  def start(): Unit = {
    if (isPaused && (elapsedTime <= maxTime)) {
      startTime = System.currentTimeMillis().toDuration

      // animate the digits while max time has not been reach
      repeat(initDelay = 1.second ,period = 1.second) {
        if (elapsedTime <= maxTime) animateTick(elapsedTime)
        else stop()
      }

      // animate progress wheel
      repeat(period = 100.millis) {
        val perc = (maxTime - elapsedTime).toFloat / maxTime
        if (perc >= 0) {
          progressWheel.setProgress(perc)
          progressWheel.setBarColor(Color.HSVToColor(Array(perc * 120, 1f, 1f))) // shift from green to red as the wheel decreases
        }
      }
      isPaused = false
    }
  }

  /**
   * Reset this stopwatch to 0:00:00. This can be called while the
   * stopwatch is running or stopped. Afterwards, this stopwatch will
   * be in the stop state.
   */
  def reset(): Unit = {
    stop()
    if (accumulatedTime.getMillis != 0) {
      accumulatedTime = 0.millis
      delay(500.millis) {
        // set to 0:00:00
        List(sTv, ssTv, mTv, mmTv, hTv).foreach(_.animate(0).setDuration(500).start())

        progressWheel.setBarColor(Color.HSVToColor(Array(120f, 1f, 1f))) // green
        progressWheel.setProgress(1)
      }
    }
  }

  /**
   * Set the max time which this stopwatch will automatically stop once reached.
   *
   * @param sec the max time in seconds. Cannot be greater than 9hr 59min 59s (35999s).
   */
  def maxTime_=(sec: Int) = {
    if (sec > (9.hours + 59.minutes + 59.seconds).seconds) {
      warn("maxTime cannot be greater than 9hr 59min 59s; setting maxTime to said limit.")
      _maxTime = 9.hours + 59.minutes + 59.seconds
    } else {
      _maxTime = sec.seconds.toDuration
    }
  }

  /** @return the set max time in milliseconds */
  def maxTime: Long = _maxTime.getMillis

  private def animateTick(millis: Long): Unit = {
    val s = millis / 1000
    //               V
    // animate 0:00:00 per second
    sTv.animate(((s - 1) % 10).toInt, (s % 10).toInt).setDuration(500).start()

    //              V
    // animate 0:00:00 per 10 seconds
    if ((s-1)/10 != s/10) ssTv.animate(((s/10 - 1) % 6).toInt, ((s/10) % 6).toInt).setDuration(500).start()

    //            V
    // animate 0:00:00 per minute
    if ((s-1)/60 != s/60) mTv.animate(((s/60 - 1) % 10).toInt, ((s/60) % 10).toInt).setDuration(500).start()

    //           V
    // animate 0:00:00 per 10 minutes
    if ((s-1)/600 != s/600) mmTv.animate(((s/600 - 1) % 6).toInt, ((s/600) % 6).toInt).setDuration(500).start()

    //         V
    // animate 0:00:00 per hour
    if ((s-1)/3600 != s/3600) hTv.animate(((s/3600 - 1) % 10).toInt, ((s/3600) % 10).toInt).setDuration(500).start()
  }
}