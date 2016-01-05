package com.uta.mars

import android.os.Bundle
import android.support.v7.widget.CardView
import android.view.{View, ViewAnimationUtils}
import android.widget.ImageView
import com.dd.morphingbutton.MorphingButton
import com.dd.morphingbutton.impl.LinearProgressButton
import com.github.clans.fab.FloatingActionButton
import com.uta.mars.common._
import io.codetail.animation.arcanimator.{ArcAnimator, Side}
import org.scaloid.common._

import com.github.nscala_time.time.Imports._

class RegistrationAct extends BaseActivity {

  private lazy val regFAB    = find[FloatingActionButton](R.id.fab_reg)
  private lazy val regForm   = find[CardView](R.id.reg_form)
  private lazy val loginForm = find[CardView](R.id.login_form)
  private lazy val nextBtn   = find[LinearProgressButton](R.id.reg_next_btn)
  private lazy val cancelBtn = find[ImageView](R.id.cancel)

  override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_reg)

    def square = MorphingButton.Params.create().duration(1).cornerRadius(2.dip).width(100.dip).height(56.dip)
      .color(R.color.accent.r2Color).colorPressed(R.color.md_orange_700.r2Color).text(R.string.login.r2str)

    find[LinearProgressButton](R.id.login_btn).morph(square)
    nextBtn.morph(square.color(R.color.md_white.r2Color).text(R.string.next.r2str))

    cancelBtn.onClick((v: View) => setupReturnTransition())
    setupEnterTransition()
  }

  override def onBackPressed(): Unit = {
    setupReturnTransition()
  }

  private def setupEnterTransition(): Unit = {
    val trans = R.transition.login_to_reg.r2Trans

    trans.onTransitionStart { (trans, listener) =>
      ArcAnimator.createArcAnimator(regFAB, regForm, 90, Side.RIGHT)
        .setDuration(500)
        .start()

      loginForm.startAnimation(R.anim.login_form_to_bg.r2anim)

      delay(600.millis) {
        regForm.visibility = View.VISIBLE
        ViewAnimationUtils.createCircularReveal(regForm, regForm.centerX, regForm.centerY, 56, regForm.getHeight)
          .setDuration(1000)
          .onAnimationStart(_ => regFAB.visibility = View.INVISIBLE)
          .start()
      }
    }

    getWindow.setSharedElementEnterTransition(trans)
  }

  private def setupReturnTransition(): Unit = {
    ViewAnimationUtils.createCircularReveal(regForm, regForm.centerX, regForm.centerY, regForm.getHeight, 56)
      .setDuration(1000)
      .onAnimationStart(_ => loginForm.startAnimation(R.anim.login_form_to_fg.r2anim))
      .onAnimationEnd(_ => regForm.visibility = View.INVISIBLE)
      .onAnimationEnd(_ => regFAB.visibility = View.VISIBLE)
      .start()

    val returnTrans = R.transition.reg_to_login.r2Trans
    getWindow.setSharedElementReturnTransition(returnTrans)

    delay(650.millis)(finishAfterTransition())
  }
}
