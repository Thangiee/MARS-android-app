package com.uta.mars.common

import android.view.View
import org.scaloid.common._

trait STraitView extends TraitView[View] with TagUtil {
  self: View =>

  override def basis: View = self
}

