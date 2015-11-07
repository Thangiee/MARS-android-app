package com.uta.mars

import android.os.Bundle
import android.support.v7.widget.CardView
import android.view.{View, ViewAnimationUtils}
import android.widget.ImageView
import com.github.clans.fab.FloatingActionButton
import com.uta.mars.common.{SBaseActivity, _}
import io.codetail.animation.arcanimator.{ArcAnimator, Side}
import org.scaloid.common._

class RegistrationAct extends SBaseActivity {

  private lazy val regFAB    = find[FloatingActionButton](R.id.fab_reg)
  private lazy val regForm   = find[CardView](R.id.reg_form)
  private lazy val loginForm = find[CardView](R.id.login_form)
  private lazy val cancelBtn = find[ImageView](R.id.cancel)

  override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_reg)

    cancelBtn.onClick((v: View) => setupReturnTransition())

    setupEnterTransition()
  }

  override def onBackPressed(): Unit = {
    setupReturnTransition()
  }

  private def setupEnterTransition(): Unit = {
    val trans = R.transition.login_to_reg.r2Trans

    trans.onTransitionStart { (trans, listener) =>
      ArcAnimator.createArcAnimator(regFAB, loginForm, 90, Side.RIGHT)
        .setDuration(500)
        .start()

      loginForm.startAnimation(R.anim.login_form_to_bg.r2anim)

      delay(600) {
        regForm.visibility = View.VISIBLE
        ViewAnimationUtils.createCircularReveal(regForm, regForm.centerX, regForm.centerY, 40, regForm.getWidth)
          .setDuration(500)
          .onAnimationStart(_ => regFAB.visibility = View.INVISIBLE)
          .start()
      }
    }

    getWindow.setSharedElementEnterTransition(trans)
  }

  private def setupReturnTransition(): Unit = {
    ViewAnimationUtils.createCircularReveal(regForm, regForm.centerX, regForm.centerY, regForm.getWidth, 40)
      .setDuration(500)
      .onAnimationStart(_ => loginForm.startAnimation(R.anim.login_form_to_fg.r2anim))
      .onAnimationEnd(_ => regForm.visibility = View.INVISIBLE)
      .onAnimationEnd(_ => regFAB.visibility = View.VISIBLE)
      .start()

    val returnTrans = R.transition.reg_to_login.r2Trans
    getWindow.setSharedElementReturnTransition(returnTrans)

    delay(500)(finishAfterTransition())
  }
}
