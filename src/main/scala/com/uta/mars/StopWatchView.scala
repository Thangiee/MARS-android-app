package com.uta.mars

import android.content.Context
import android.graphics.{Canvas, Color, Paint}
import android.util.AttributeSet
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.Toast
import com.github.adnansm.timelytextview.TimelyView
import com.github.adnansm.timelytextview.model.NumberUtils

class StopWatchView(ctx: Context, attrs: AttributeSet) extends ViewGroup(ctx, attrs) {

  private val sTv  = new TimelyView(ctx)
  private val ssTv = new TimelyView(ctx)
  private val mTv  = new TimelyView(ctx)
  private val mmTv = new TimelyView(ctx)

  private val mPaint = new Paint


  override def onAttachedToWindow(): Unit = {
    super.onAttachedToWindow()

    mPaint.setAntiAlias(true)
    mPaint.setColor(Color.WHITE)
    mPaint.setStrokeWidth(8.0f)
    mPaint.setStyle(Paint.Style.FILL)

    List(sTv, ssTv, mTv, mmTv).foreach { tv =>
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

    println(s">> $digitW, $w")

    sTv.layout(w - digitW, 0, w, h)
    ssTv.layout(w - (1.8*digitW).toInt, 0, w, h)

    mTv.layout(w/2 + digitW, 0, w, h)
  }


  override def dispatchDraw(canvas: Canvas): Unit = {
    super.dispatchDraw(canvas)
    canvas.drawCircle(400, 55, 10, mPaint)
    canvas.drawCircle(400, 165, 10, mPaint)
  }

  override def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): Unit = {
    val w = MeasureSpec.makeMeasureSpec(200, MeasureSpec.AT_MOST)
    val h = MeasureSpec.makeMeasureSpec(220, MeasureSpec.AT_MOST)
    measureChildren(w, h)
    setMeasuredDimension(800, 400)
  }
}

