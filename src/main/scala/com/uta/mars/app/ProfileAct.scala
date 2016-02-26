package com.uta.mars.app

import android.app.Activity
import android.content.{Context, Intent}
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.graphics.drawable.{RoundedBitmapDrawable, RoundedBitmapDrawableFactory}
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.{LinearLayoutManager, RecyclerView, Toolbar}
import android.view.{MenuItem, View}
import android.widget.{Chronometer, ImageView, TextView}
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.github.clans.fab.FloatingActionButton
import com.github.florent37.viewanimator.ViewAnimator
import com.makeramen.roundedimageview.RoundedImageView
import com.skocken.efficientadapter.lib.adapter.EfficientRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import com.uta.mars.R
import com.uta.mars.api.Assistant
import com.uta.mars.app.common._
import jp.satorufujiwara.scrolling.behavior.ParallaxBehavior
import jp.satorufujiwara.scrolling.{Behavior, MaterialScrollingLayout}
import org.scaloid.common._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class ProfileAct extends BaseActivity {

  private lazy val toolbar      = find[Toolbar](R.id.toolbar)
  private lazy val editFAB      = find[FloatingActionButton](R.id.fab)
  private lazy val recyclerView = find[RecyclerView](R.id.recyclerView)
  private lazy val asstNameTv   = find[TextView](R.id.titleText)
  private lazy val faceImgView  = find[RoundedImageView](R.id.face_img)
  private lazy val bgImgView    = find[ImageView](R.id.bg_image)

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_profile)
    setTitle("")

    // make the status bar transparent and the background image visible through it
    getWindow.getDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    getWindow.setStatusBarColor(R.color.transparent_status_bar)

    toolbar.setBackgroundColor(android.R.color.transparent.r2Color)
    setSupportActionBar(toolbar)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)

    // add custom effects that will animate as the user scroll up/down
    val scrollingLayout = find[MaterialScrollingLayout](R.id.root)
    scrollingLayout.addBehavior(bgImgView, new ParallaxBehavior())
    scrollingLayout.addBehavior(find(R.id.face_img), new ParallaxBehavior())
    scrollingLayout.addBehavior(find(R.id.overlayView), new OverlayBehavior())
    scrollingLayout.addBehavior(asstNameTv, new NameBehavior())
    scrollingLayout.addBehavior(editFAB, new FabBehavior())

    // load assistant face image
    MarsApi.faceImages().map(_.headOption.foreach(img =>
      runOnUiThread {
        Glide.`with`(ctx)
          .load(img.url+s"?size=${72.dipToPixels.toInt}")
          .asBitmap()
          .placeholder(R.drawable.ic_launcher)
          .animate(android.R.anim.fade_in)
          .centerCrop()
          .into(new BitmapImageViewTarget(faceImgView) {
            override def setResource(resource: Bitmap): Unit = {
              val drawable = RoundedBitmapDrawableFactory.create(getResources, resource)
              drawable.setCircular(true)
              faceImgView.setImageDrawable(drawable)
            }
          })
      }
    ))

    // load header background image
    Glide.`with`(ctx).load(R.drawable.uta).thumbnail(0.1f).into(bgImgView)

    editFAB.onClick(startActivityForResult(ProfileEditAct(), 0))
  }

  override def onResume(): Unit = {
    super.onResume()
    MarsApi.assistantInfo()
      .map(asst => runOnUiThread {
        asstNameTv.setText(s"${asst.firstName} ${asst.lastName}")
        val adapter = new EfficientRecyclerAdapter[Assistant](R.layout.asst_profile_info, classOf[AsstViewHolder], Seq(asst))
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx))
        recyclerView.setAdapter(adapter.asInstanceOf[Adapter[AsstViewHolder]])
      })
      .badMap { case Err(code, msg) =>
        logger.warn(s"Expected MarsApi.assistantInfo to be cached by HomeAct but got: $code, $msg")
        showApiErrorDialog(code)
      }
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    if (resultCode == Activity.RESULT_OK) Snackbar.make(recyclerView, "Profile Updated", 3000).show()
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    if (item.getItemId == android.R.id.home) finish()
    super.onOptionsItemSelected(item)
  }
}

private class AsstViewHolder(v: View) extends EfficientViewHolder[Assistant](v) {
  def findTextView(id: Int): TextView = findViewByIdEfficient[TextView](id)
  def updateView(context: Context, asst: Assistant): Unit = {
    findTextView(R.id.tv_email).text = asst.email
    findTextView(R.id.tv_net_id).text = asst.netId
    findTextView(R.id.tv_emp_id).text = asst.employeeId
    findTextView(R.id.tv_pay_rate).text = f"${asst.rate}%.2f per hours"
    findTextView(R.id.tv_job).text = asst.job
    findTextView(R.id.tv_department).text = asst.department
    findTextView(R.id.tv_title).text = asst.title
    findTextView(R.id.tv_title_code).text = asst.titleCode
  }
}

private class OverlayBehavior(implicit ctx: Context) extends Behavior {
  override def onScrolled(view: View, scrollY: Int, dy: Int): Unit = {
    val movingHeight = getFlexibleHeight - 80.dip
    ViewCompat.setTranslationY(view, -(scrollY min movingHeight))
    if (scrollY >= movingHeight) view.setAlpha(1) else view.setAlpha(scrollY.toFloat / movingHeight)
  }
}

private class NameBehavior(implicit ctx: Context) extends Behavior {
  val animationToggleHeight = (240 - 56*2 - 24).dipToPixels
  var isTextSizeToggled     = false

  override def onScrolled(view: View, scrollY: Int, dy: Int): Unit = {
    ViewCompat.setTranslationY(view, -(scrollY min 132.dip))

    if (!isTextSizeToggled && scrollY >= animationToggleHeight) {
      isTextSizeToggled = true
      ViewAnimator.animate(view).property("textSize", 34, 24).accelerate().duration(200).start() // text size 34 to 24
    } else if (isTextSizeToggled && scrollY < animationToggleHeight) {
      isTextSizeToggled = false
      ViewAnimator.animate(view).property("textSize", 24, 34).accelerate().duration(200).start() // text size 24 to 34
    }
  }
}

private class FabBehavior(implicit ctx: Context) extends Behavior {
  val animationToggleHeight = (240 - 56*2 - 24).dipToPixels
  var isFabVisible          = false

  override def onScrolled(view: View, scrollY: Int, dy: Int): Unit = {
    ViewCompat.setTranslationY(view, -(scrollY min 156.dip))

    if (!isFabVisible && scrollY >= animationToggleHeight) {
      isFabVisible = true
      ViewAnimator.animate(view).scale(1, 0).accelerate().duration(200).start()
    } else if (isFabVisible && scrollY < animationToggleHeight) {
      isFabVisible = false
      ViewAnimator.animate(view).scale(0, 1).accelerate().duration(200).start()
    }
  }
}