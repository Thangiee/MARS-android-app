package com.uta.mars.common

import android.util.AttributeSet
import android.view.ViewGroup
import org.scaloid.common._

trait STraitViewGroup extends TraitViewGroup[ViewGroup] with TagUtil {
  self: ViewGroup =>

  override def basis: ViewGroup = self

  implicit class AttrConversion(res: (Array[Int], Int)) {

    def r2PxSize(attrSet: AttributeSet, defVal: Int = 0): Int = res match {
      case (attrs, index) =>
        val typedArray = context.obtainStyledAttributes(attrSet, attrs)
        val pxSize = typedArray.getDimensionPixelSize(index, defVal)
        typedArray.recycle()
        pxSize
    }
  }
}

