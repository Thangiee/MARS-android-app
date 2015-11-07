package com.uta.mars

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.github.clans.fab.FloatingActionButton
import com.uta.mars.common._
import org.scaloid.common._

class LoginAct extends SBaseActivity {

  private lazy val registrationFAB = find[FloatingActionButton](R.id.fab_reg)

  override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_login)

    registrationFAB.onClick { (v: View) =>
      val i = new Intent(this, classOf[RegistrationAct])
      val transOpt = ActivityOptions.makeSceneTransitionAnimation(this, v, R.string.shared_fab_reg.r2str)
      startActivity(i, transOpt.toBundle)
    }
  }

}
