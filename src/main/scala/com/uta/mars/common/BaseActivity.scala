package com.uta.mars.common

import android.content.Context
import android.support.v7.app.AppCompatActivity

trait BaseActivity extends AppCompatActivity with org.scaloid.common.SActivity with Base {
  override def getCtx: Context = ctx
}
