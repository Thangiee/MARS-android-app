package com.uta.mars.app

import android.app.Activity
import android.content.{Context, Intent}
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images
import android.support.v7.widget.Toolbar
import android.widget.{Button, Toast}
import com.pnikosis.materialishprogress.ProgressWheel
import com.uta.mars.R
import com.uta.mars.app.common._
import org.scaloid.common._

class FaceRecogSetupAct extends BaseActivity {
  private lazy val toolbar       = find[Toolbar](R.id.toolbar)
  private lazy val takePhotoBtn  = find[Button](R.id.btn_take_photo)
  private lazy val progressWheel = find[ProgressWheel](R.id.progress_wheel)

  private val FACE_DETECT_REQUEST: Int = 101

  private var numPhotoNeeded = 5

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_setup_face_recog)
    setSupportActionBar(toolbar)
    setTitle("Face Recognition Setup")

    takePhotoBtn.onClick(startActivityForResult(new Intent(ctx, classOf[FaceDetectionAct]), FACE_DETECT_REQUEST))
    progressWheel.setInvisible()

    numPhotoNeeded = getIntent.getIntExtra("photo-needed", 5)
    Toast.makeText(ctx, s"Need $numPhotoNeeded face photos", Toast.LENGTH_LONG).show()
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    (requestCode, resultCode) match {
      case (FACE_DETECT_REQUEST, Activity.RESULT_OK) =>
        progressWheel.setVisible()
        Toast.makeText(ctx, "Analyzing...", Toast.LENGTH_LONG).show()

        // load the face image captured by FaceDetectionAct
        val faceUri = data.getParcelableExtra[Uri](FACE_IMG_KEY)
        val faceImg = Images.Media.getBitmap(ctx.contentResolver, faceUri).toBytes

        MarsApi.addFaceForRecognition(faceImg)
          .map(_ => runOnUiThread {
            progressWheel.setInvisible()
            numPhotoNeeded -= 1
            if (numPhotoNeeded <= 0) finish()
            else Toast.makeText(ctx, s"$numPhotoNeeded more photos", Toast.LENGTH_LONG).show()
          })
          .badMap {
            case Err(400, msg)  =>
              runOnUiThread(progressWheel.setInvisible())
              new AlertDialogBuilder("Bad Photo", msg) { positiveButton("Ok") } show()
            case Err(413, _)  =>
              runOnUiThread(progressWheel.setInvisible())
              new AlertDialogBuilder("Image too large", "Try moving the camera farther away.") { positiveButton("Ok") } show()
            case Err(code, _) =>
              runOnUiThread(progressWheel.setInvisible())
              showApiErrorDialog(code)
          }

        ctx.getContentResolver.delete(faceUri, null, null) // delete the photo locally afterwords

      case _ =>
        logger.error(s"Unexpected State: requestCode: $requestCode, resultCode: $resultCode")
        progressWheel.setInvisible()
        super.onActivityResult(requestCode, resultCode, data)
    }
  }

  override def onResume(): Unit = {
    super.onResume()
    if (numPhotoNeeded <= 0) finish()
  }
}

object FaceRecogSetupAct {
  def apply(numPhotoNeeded: Int)(implicit ctx: Context): Intent = {
    new Intent(ctx, classOf[FaceRecogSetupAct]).args("photo-needed" -> numPhotoNeeded)
  }
}
