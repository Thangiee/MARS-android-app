package com.uta.mars.app

import android.content.{Context, Intent}
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.{Menu, MenuItem}
import android.widget.RadioGroup
import com.rengwuxian.materialedittext.MaterialEditText
import com.rengwuxian.materialedittext.validation.RegexpValidator
import com.uta.mars.R
import com.uta.mars.api.Assistant
import com.uta.mars.app.common._
import org.scaloid.common.AlertDialogBuilder

class RegistrationDetail extends BaseActivity {

  private lazy val toolbar      = find[Toolbar](R.id.toolbar)
  private lazy val netIdEt      = find[MaterialEditText](R.id.et_net_id)
  private lazy val empIdEt      = find[MaterialEditText](R.id.et_emp_id)
  private lazy val usernameEt   = find[MaterialEditText](R.id.et_username)
  private lazy val passwordEt   = find[MaterialEditText](R.id.et_password)
  private lazy val emailEt      = find[MaterialEditText](R.id.et_email)
  private lazy val deptEt       = find[MaterialEditText](R.id.et_dept)
  private lazy val rateEt       = find[MaterialEditText](R.id.et_rate)
  private lazy val titleEt      = find[MaterialEditText](R.id.et_title)
  private lazy val titleCodeEt  = find[MaterialEditText](R.id.et_title_code)
  private lazy val jobTypeGroup = find[RadioGroup](R.id.rg_job_type)
  private lazy val allEditTexts = Seq(netIdEt, empIdEt, usernameEt, passwordEt, emailEt, deptEt, rateEt, titleEt, titleCodeEt)

  private lazy val firstName = getIntent.getStringExtra("first")
  private lazy val lastName  = getIntent.getStringExtra("last")

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_registration)
    setSupportActionBar(toolbar)
    setTitle("Registration")
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)

    val noBlank = new RegexpValidator("Can't be blank", ".+")
    val noSpace = new RegexpValidator("Can't have spaces", "^\\s*\\S*$")

    allEditTexts.map(_.addValidator(noBlank))
    netIdEt.addValidator(noSpace)
    empIdEt.addValidator(noSpace)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.register, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.menu_register =>
        if (allEditTexts.forall(_.validate())) doRegisitration()
        true

      case _ /* no match */ => super.onOptionsItemSelected(item)
    }
  }

  private def doRegisitration(): Unit = {
    val job = jobTypeGroup.getCheckedRadioButtonId match {
      case R.id.rb_teaching => "teaching"
      case R.id.rb_grading  => "grading"
      case _                => ""
    }

    val asst = Assistant(
      rateEt.txt2Double, netIdEt.txt2Str, emailEt.txt2Str,
      job, deptEt.txt2Str, lastName, firstName, empIdEt.txt2Str,
      threshold=0.4, titleEt.txt2Str, titleCodeEt.txt2Str
    )

    MarsApi.createAcc(usernameEt.txt2Str, passwordEt.txt2Str, asst)
      .map(_ => {
        new AlertDialogBuilder("Account Created", "You can now login with this account.") {
          positiveButton("Login", { finish(); startActivity[LoginAct] })
        }.show()
      })
      .badMap {
        case Err(400, msg) => new AlertDialogBuilder("Bad Input", msg) { positiveButton("Back") } show()
        case Err(409, msg) => new AlertDialogBuilder("Conflict", msg) { positiveButton("Back") } show()
        case Err(code, _)  => showApiErrorDialog(code)
      }
  }
}

object RegistrationDetail {
  def apply(firstName: String, lastName: String)(implicit ctx: Context): Intent = {
    new Intent(ctx, classOf[RegistrationDetail]).args("first" -> firstName, "last" -> lastName)
  }
}
