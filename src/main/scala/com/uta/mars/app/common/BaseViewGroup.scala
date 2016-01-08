package com.uta.mars.app.common

import android.content.Context
import android.view.ViewGroup
import org.scaloid.common.{TagUtil, TraitViewGroup}

trait BaseViewGroup extends TraitViewGroup[ViewGroup] with Base with TagUtil {
  self: ViewGroup =>

  override implicit val getCtx: Context = context
  override def basis: ViewGroup = self
}
