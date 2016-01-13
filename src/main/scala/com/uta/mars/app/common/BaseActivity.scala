package com.uta.mars.app.common

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.uta.mars.app.LoginAct
import org.scaloid.common.AlertDialogBuilder

trait BaseActivity extends AppCompatActivity with org.scaloid.common.SActivity with Base {
  override def getCtx: Context = ctx

  def showReLoginDialog(): Unit =
    new AlertDialogBuilder("Expired Credentials", "Your credentials may have expired. Please relog in.")(getCtx) {
      negativeButton(android.R.string.cancel.r2str)
      positiveButton("Login", { finish(); startActivity[LoginAct] })
    }.show()

}
