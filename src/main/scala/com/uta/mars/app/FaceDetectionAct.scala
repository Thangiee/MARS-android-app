package com.uta.mars.app

import android.app.Activity
import android.content.Intent
import android.hardware.Camera
import android.hardware.Camera.{CameraInfo, Face, PictureCallback}
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images
import android.support.v7.widget.Toolbar
import android.view.{MenuItem, Surface, SurfaceHolder, SurfaceView}
import android.widget.ImageView
import cat.lafosca.facecropper.FaceCropper
import com.blundell.woody.Woody
import com.blundell.woody.core.FaceDetectionCamera
import com.uta.mars.R
import com.uta.mars.app.common._
import org.scaloid.common._

class FaceDetectionAct extends BaseActivity with Woody.ActivityListener with Woody.ActivityMonitorListener with SurfaceHolder.Callback with PictureCallback {
  private lazy val toolbar = find[Toolbar](R.id.toolbar)

  private var surfaceHolder: Option[SurfaceHolder] = None

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_face_dect)
    setTitle("Face Recognition")
    setSupportActionBar(toolbar)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)

    find[SurfaceView](R.id.surface_view).getHolder.addCallback(this)
    Woody.onCreateLoad(this)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    if (item.getItemId == android.R.id.home) finish()
    super.onOptionsItemSelected(item)
  }

  override def onLoaded(camera: FaceDetectionCamera): Unit = {
    surfaceHolder.foreach(holder => camera.initialise(this, holder))
    onDestroy(camera.recycle())
  }

  override def onFaceDetected(faces: Array[Face], camera: Camera): Unit = {
    val degrees = getWindowManager.getDefaultDisplay.getRotation match {
      case Surface.ROTATION_0   => 0
      case Surface.ROTATION_90  => 90
      case Surface.ROTATION_180 => 180
      case Surface.ROTATION_270 => 270
      case _                    => 0
    }

    val info = new CameraInfo()
    Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info)

    val param = camera.getParameters
    param.setRotation((info.orientation - degrees + 360) % 360)
    camera.setParameters(param)
    camera.enableShutterSound(true)
    camera.takePicture(null, null, this)
  }

  override def onPictureTaken(data: Array[Byte], camera: Camera): Unit = {
    play(R.raw.music_marimba_chord.r2Uri)

    val croppedFace = new FaceCropper().getCroppedImage(data.toBitmap)
    val view = getLayoutInflater.inflate(R.layout.image_view_container, null)
    view.find[ImageView](R.id.image_view).setImageDrawable(croppedFace.toDrawable)

    new AlertDialogBuilder("Is this correct?") {
      setView(view)
      setCancelable(false)
      negativeButton("Try again", {
        camera.startPreview()
        camera.startFaceDetection()
      })
      positiveButton("Yes", {
        val uri = Uri.parse(Images.Media.insertImage(ctx.getContentResolver, croppedFace, "face", null))
        FaceDetectionAct.this.setResult(Activity.RESULT_OK, new Intent().putExtra(FACE_IMG_KEY, uri))
        FaceDetectionAct.this.finish()
      })
    }.show()
  }

  override def onFaceDetectionNonRecoverableError(): Unit = {
    new AlertDialogBuilder(_message = "Unexpected error occurred while trying to detect face.") {
      positiveButton("Exit", finish())
    }.show()
  }

  override def onFailedToLoadFaceDetectionCamera(): Unit = {
    new AlertDialogBuilder(_message = "Unable to start the camera.") {
      positiveButton("Exit", finish())
    }.show()
  }

  override def onFaceTimedOut(): Unit = {}

  override def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int): Unit = surfaceHolder = Some(holder)

  override def surfaceCreated(holder: SurfaceHolder): Unit = surfaceHolder = Some(holder)

  override def surfaceDestroyed(holder: SurfaceHolder): Unit = {}

}
