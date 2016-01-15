package com.uta.mars.app

import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import com.dlazaro66.qrcodereaderview.QRCodeReaderView.OnQRCodeReadListener
import com.uta.mars.R
import com.uta.mars.app.common._
import org.scaloid.common._

class QrCodeScanAct extends BaseActivity with OnQRCodeReadListener {

  private lazy val scannerView = find[QRCodeReaderView](R.id.qr_decoder_view)
  private lazy val toolbar     = find[Toolbar](R.id.toolbar)

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_scanner)
    setTitle("QR Code Scanner")
    setSupportActionBar(toolbar)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
    scannerView.setOnQRCodeReadListener(this)
  }

  override def onResume(): Unit = {
    super.onResume()
    scannerView.getCameraManager.startPreview()
  }

  override def onPause(): Unit = {
    super.onPause()
    scannerView.getCameraManager.stopPreview()
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    if (item.getItemId == android.R.id.home) finish()
    super.onOptionsItemSelected(item)
  }

  override def cameraNotFound(): Unit = {
    new AlertDialogBuilder(_message = "Unable to start the camera.") {
      positiveButton("Exit", finish())
    }.show()
  }

  override def onQRCodeRead(code: String, pointFs: Array[PointF]): Unit = {
    logger.debug(s"QR code scanner read: $code")
    val returnIntent = new Intent()
    returnIntent.putExtra(QR_CODE_KEY, code)
    setResult(Activity.RESULT_OK, returnIntent)
    finish()
  }

  override def QRCodeNotFoundOnCamImage(): Unit = { }
}