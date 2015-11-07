package com.uta.mars

import android.content.Context
import android.support.v7.widget.CardView
import com.github.clans.fab.FloatingActionButton
import com.uta.mars.common.{STraitView, STraitViewGroup}

package object customviews {

  case class SFloatingActionButton(implicit ctx: Context) extends FloatingActionButton(ctx) with STraitView

  class SCardView(implicit ctx: Context) extends CardView(ctx) with STraitViewGroup

}
