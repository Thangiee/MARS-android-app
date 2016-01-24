package com.uta.mars.app

import android.content.Context
import android.util.AttributeSet
import com.github.adnansm.timelytextview.TimelyView

class STimelyView(ctx: Context, attrs: AttributeSet) extends TimelyView(ctx, attrs) {

  override def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): Unit = {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    val width = getMeasuredWidth
    val height = (getMeasuredHeight * 1.05).toInt  // fix bottom clipping
    val widthWithoutPadding = width - getPaddingLeft - getPaddingRight
    val heigthWithoutPadding = height - getPaddingTop - getPaddingBottom

    val maxWidth = heigthWithoutPadding * 1.0f
    val maxHeight = widthWithoutPadding / 1.0f

    if (widthWithoutPadding > maxWidth) {
      setMeasuredDimension(maxWidth.toInt + getPaddingLeft + getPaddingRight, height)
    } else {
      setMeasuredDimension(width, maxHeight.toInt + getPaddingTop + getPaddingBottom)
    }

    setMeasuredDimension(width, height)
  }
}
