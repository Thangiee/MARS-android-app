package com.uta.mars

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.dd.morphingbutton.impl.LinearProgressButton
import com.github.clans.fab.FloatingActionButton
import com.rengwuxian.materialedittext.MaterialEditText
import com.uta.mars.common._
import org.scaloid.common._
import com.github.nscala_time.time.Imports._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LoginAct extends BaseActivity {

  private lazy val registrationFAB = find[FloatingActionButton](R.id.fab_reg)
  private lazy val loginBtn = find[LinearProgressButton](R.id.login_btn)
  private lazy val usernameEt = find[MaterialEditText](R.id.et_username)
  private lazy val passwordEt = find[MaterialEditText](R.id.et_password)

  override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_login)

    loginBtn.morphToNormalBtn(R.string.login.r2str)
    loginBtn.onClick { (v: View) =>
      loginBtn.morphToProgress(R.color.material_grey_300.r2Color, R.color.md_light_blue_500.r2Color, 5, 300, 10, 500)
      delay(500.millis)(doLogin())
    }

    registrationFAB.onClick((v: View) => doRegistration())
  }

  private def doRegistration(): Unit = {
    val i = new Intent(this, classOf[RegistrationAct])
    val transOpt = ActivityOptions.makeSceneTransitionAnimation(this, registrationFAB, R.string.shared_fab_reg.r2str)
    startActivity(i, transOpt.toBundle)
  }

  private def doLogin(): Unit = {
    fillProgressBar(0, 50, 500.millis)
    delay(500.millis) {
      MarsApi.login(usernameEt.text.toString, passwordEt.text.toString).map {
        case Ok(cookies)    => super.session.saveAuthnCookies(cookies); goToHomeAct()
        case Err(403, msg)  => showInvalidUserOrPass()
        case Err(code, msg) => showApiErrorDialog(code, msg); runOnUiThread(loginBtn.morphToErrorBtn())
      }
    }

    def goToHomeAct(): Unit = runOnUiThread {
      fillProgressBar(50, 100, 500.millis)
      delay(550.millis)(loginBtn.morphToSuccessBtn())
      delay(1050.millis) {
        startActivity[HomeAct]
        finish()
      }
    }

    def showInvalidUserOrPass(): Unit = runOnUiThread {
      fillProgressBar(50, 100, 500.milli)
      delay(550.millis) {
        Seq(usernameEt, passwordEt).foreach(_.setError("Invalid username or password"))
        loginBtn.morphToErrorBtn()
      }
    }

    def fillProgressBar(startPercent: Int, endPercent: Int, duration: Duration): Unit = {
      var progress = startPercent
      val period =  duration.getMillis / (endPercent - startPercent)

      Future {
        while (progress < endPercent) {
          progress += 1
          runOnUiThread(loginBtn.setProgress(progress))
          Thread.sleep(period)
        }
      }
    }
  }

}
