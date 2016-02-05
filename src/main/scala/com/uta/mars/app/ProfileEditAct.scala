package com.uta.mars.app

import android.app.Activity
import android.content.{Context, Intent}
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.{MenuItem, Menu}
import com.rengwuxian.materialedittext.MaterialEditText
import com.rengwuxian.materialedittext.validation.RegexpValidator
import com.uta.mars.R
import com.uta.mars.api.MarsApi
import com.uta.mars.app.common._
import org.scaloid.common.AlertDialogBuilder

class ProfileEditAct extends BaseActivity {

  private lazy val toolbar      = find[Toolbar](R.id.toolbar)
  private lazy val deptEt       = find[MaterialEditText](R.id.et_dept)
  private lazy val rateEt       = find[MaterialEditText](R.id.et_rate)
  private lazy val titleEt      = find[MaterialEditText](R.id.et_title)
  private lazy val titleCodeEt  = find[MaterialEditText](R.id.et_title_code)
  private lazy val allEditTexts = Seq(deptEt, rateEt, titleEt, titleCodeEt)

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_profile_edit)
    setSupportActionBar(toolbar)
    setTitle("Edit Profile")
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)

    val noBlank = new RegexpValidator("Can't be blank", ".+")
    allEditTexts.map(_.addValidator(noBlank))

    MarsApi.assistantInfo().map(asst => runOnUiThread {
      deptEt.setText(asst.department)
      rateEt.setText(asst.rate.toString)
      titleEt.setText(asst.title)
      titleCodeEt.setText(asst.titleCode)
    })
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.register, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.menu_register => if (allEditTexts.forall(_.validate())) { updateAssistant() }; true
      case android.R.id.home  => finish(); true
      case _ /* no match */   => super.onOptionsItemSelected(item)
    }
  }

  def updateAssistant(): Unit = {
    MarsApi.updateAssistant(rateEt.txt2Double, deptEt.txt2Str, titleEt.txt2Str, titleCodeEt.txt2Str)
      .map(_ => {
        setResult(Activity.RESULT_OK)
        finish()
      })
      .badMap {
        case Err(400, msg) => new AlertDialogBuilder("Bad Input", msg) { positiveButton("Back") } show()
        case Err(code, _)  => showApiErrorDialog(code)
      }
  }
}

object ProfileEditAct {
  def apply()(implicit ctx: Context): Intent = new Intent(ctx, classOf[ProfileEditAct])
}
