package com.uta.mars.app

import android.app.Activity
import android.content.{Context, Intent}
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.{Button, TextView, Toast}
import com.pnikosis.materialishprogress.ProgressWheel
import com.uta.mars.R
import com.uta.mars.app.common._
import org.scaloid.common._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ClockInOutAct extends BaseActivity {
  private lazy val toolbar       = find[Toolbar](R.id.toolbar)
  private lazy val instructionTv = find[TextView](R.id.tv_instruction)
  private lazy val startBtn      = find[Button](R.id.btn_start)
  private lazy val progressWheel = find[ProgressWheel](R.id.progress_wheel)

  private lazy val isTeachingJob = getIntent.getStringExtra("job") == "teaching"
  private lazy val isClockingIn  = getIntent.getBooleanExtra("is-clocking-in", true)

  private val QR_CODE_REQUEST     = 100
  private val FACE_DETECT_REQUEST = 101

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_clock_in_out)
    setSupportActionBar(toolbar)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
    setTitle(if (isClockingIn) "Clock In" else "Clock Out")

    progressWheel.setInvisible()
    instructionTv.setText(if (isTeachingJob) R.string.teaching_clocking_instruction.r2str else R.string.grader_clocking_instruction.r2str)
    startBtn.onClick(startActivityForResult(new Intent(ctx, classOf[FaceDetectionAct]), FACE_DETECT_REQUEST))
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    // steps for teaching job: face recognition -> scan QR code -> clock in/out
    // steps for grading job: face recognition -> clock in/out
    val Teaching = true
    val Grading = false

    (requestCode, resultCode, isTeachingJob) match {
      case (QR_CODE_REQUEST, Activity.RESULT_OK, Teaching)      =>
        val uuid = data.getStringExtra(UUID_KEY)
        val compId = data.getStringExtra(COMP_ID_KEY)

        MarsApi.verifyUUID(uuid)
          .map(succ => doClockInOut(compId))
          .badMap {
            case Err(410, _) =>
              runOnUiThread(progressWheel.setInvisible())
              new AlertDialogBuilder("Verification failed", "The scanned QR code has already expired.") {
                positiveButton("Ok")
              }.show()
            case Err(code, _) =>
              runOnUiThread(progressWheel.setInvisible())
              showApiErrorDialog(code)
          }

      case (FACE_DETECT_REQUEST, Activity.RESULT_OK, Teaching)  =>
        progressWheel.setVisible()
        Toast.makeText(ctx, "Analyzing...", Toast.LENGTH_LONG).show()
        doFaceRecognition().map(isSuccessful => {
          if (isSuccessful) startActivityForResult(new Intent(ctx, classOf[QrCodeScanAct]), QR_CODE_REQUEST)
        })

      case (FACE_DETECT_REQUEST, Activity.RESULT_OK, Grading) =>
        progressWheel.setVisible()
        Toast.makeText(ctx, "Analyzing...", Toast.LENGTH_LONG).show()
        doFaceRecognition().map(isSuccessful => if (isSuccessful) doClockInOut("Undisclosed location"))

      case (_, Activity.RESULT_CANCELED, _) =>
        progressWheel.setInvisible()

      case _ =>
        logger.error(s"Unexpected State: requestCode: $requestCode, resultCode: $resultCode, isTeaching: $isTeachingJob")
        progressWheel.setInvisible()
        new AlertDialogBuilder("Unexpected Error", "The application has encounter an unexpected error.") {
          positiveButton("Dismiss")
        }.show()
    }

    def doFaceRecognition(): Future[Boolean] = {
      // load the face image
      val faceUri = data.getParcelableExtra[Uri](FACE_IMG_KEY)
      val faceImg = Images.Media.getBitmap(ctx.contentResolver, faceUri).toBytes

      val result  = MarsApi.facialRecognition(faceImg)
        .map(result => {
          logger.debug(s"Recognition Result: $result")
          if (result.confidence >= result.threshold) true
          else {
            runOnUiThread(progressWheel.setInvisible())
            new AlertDialogBuilder("Verification failed", "You did not pass the the facial recognition check. Please try again.") {
              positiveButton("Ok")
            }.show()
            false
          }
        })
        .badMap(err => { runOnUiThread(progressWheel.setInvisible()); showApiErrorDialog(err.code); false })
        .merge

      ctx.getContentResolver.delete(faceUri, null, null) // delete the face image afterwords
      result
    }

    def doClockInOut(compId: String): Unit = {
      val response = if (isClockingIn) MarsApi.clockIn(compId) else MarsApi.clockOut(compId)
      response
        .map(succ => { setResult(Activity.RESULT_OK); finish() })
        .badMap(err => { runOnUiThread(progressWheel.setInvisible()); showApiErrorDialog(err.code) })
    }
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    if (item.getItemId == android.R.id.home) finish()
    super.onOptionsItemSelected(item)
  }
}

object ClockInOutAct {
  def apply(asstInfo: Assistant, isClockingIn: Boolean)(implicit ctx: Context): Intent = {
    new Intent(ctx, classOf[ClockInOutAct]).args("job" -> asstInfo.job, "is-clocking-in" -> isClockingIn)
  }
}
