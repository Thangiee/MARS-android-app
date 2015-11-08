package com.uta.mars

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.dd.morphingbutton.MorphingButton
import com.dd.morphingbutton.impl.IndeterminateProgressButton
import com.github.clans.fab.FloatingActionButton
import com.uta.mars.common._
import org.scaloid.common._

class LoginAct extends SBaseActivity {

  private lazy val registrationFAB = find[FloatingActionButton](R.id.fab_reg)
  private lazy val loginBtn = find[IndeterminateProgressButton](R.id.login_btn)

  override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_login)

    registrationFAB.onClick { (v: View) =>
      val i = new Intent(this, classOf[RegistrationAct])
      val transOpt = ActivityOptions.makeSceneTransitionAnimation(this, v, R.string.shared_fab_reg.r2str)
      startActivity(i, transOpt.toBundle)
    }

    val square = MorphingButton.Params.create().duration(1).cornerRadius(2.dip).width(100.dip).height(56.dip)
      .color(R.color.accent.r2Color).colorPressed(R.color.md_orange_700.r2Color).text(R.string.login.r2str)

    loginBtn.morph(square)

    loginBtn.onClick { (v: View) =>
      loginBtn.morphToProgress(R.color.md_grey_500.r2Color, 5, 300, 10, 500,
        R.color.md_red_500.r2Color, R.color.md_yellow_500.r2Color, R.color.md_green_500.r2Color, R.color.md_blue_500.r2Color)

      delay(2000) {
        val success = MorphingButton.Params.create()
          .duration(1000)
          .cornerRadius(56.dip)
          .width(56.dip)
          .height(56.dip)
          .color(R.color.md_light_green_500.r2Color)
          .icon(R.drawable.ic_done)

        loginBtn.morph(success)
      }
    }
  }

}
