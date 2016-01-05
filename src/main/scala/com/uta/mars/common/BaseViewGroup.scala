package com.uta.mars.common

import android.content.Context
import android.view.ViewGroup
import org.scaloid.common._

trait BaseViewGroup extends TraitViewGroup[ViewGroup] with Base with TagUtil {
  self: ViewGroup =>

  override implicit val getCtx: Context = context
  override def basis: ViewGroup = self
}

