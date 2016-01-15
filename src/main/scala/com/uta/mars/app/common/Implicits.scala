package com.uta.mars.app.common

import java.io.ByteArrayOutputStream

import android.animation.Animator.AnimatorListener
import android.animation.{Animator, AnimatorInflater, TimeInterpolator}
import android.content.{Intent, Context}
import android.graphics.{Bitmap, BitmapFactory}
import android.graphics.drawable.{Drawable, BitmapDrawable}
import android.transition.Transition.TransitionListener
import android.transition.{Transition, TransitionInflater}
import android.util.{AttributeSet, TypedValue}
import android.view.View
import android.view.animation.{Animation, AnimationUtils}
import com.dd.morphingbutton.MorphingButton
import com.dd.morphingbutton.impl.LinearProgressButton
import com.github.florent37.viewanimator.AnimationListener.Update
import com.uta.mars.R
import org.scaloid.common._

import scala.language.implicitConversions

object Implicits extends Implicits
object ResImplicits extends ResImplicits
object UnitImplicits extends UnitImplicits
object ViewImplicits extends ViewImplicits
object ViewAnimatorImplicits extends ViewAnimatorImplicits
object TransitionImplicits extends TransitionImplicits
object AnimatorImplicits extends AnimatorImplicits
object AttrImplicits extends AttrImplicits
object MorphingBtnImplicits extends MorphingBtnImplicits

trait Implicits extends AnyRef with ResImplicits with ViewImplicits with TransitionImplicits with AnimatorImplicits
                               with AttrImplicits with MorphingBtnImplicits with UnitImplicits with ViewAnimatorImplicits
                               with IntentImplicits with ImageImplicits

trait IntentImplicits {
  implicit class IntentOp(i: Intent) {
    def args(arguments: (String, Any)*): Intent = {
      for ((k, v) ← arguments) {
        v match {
          case v: String        => i.putExtra(k, v)
          case v: Int           => i.putExtra(k, v)
          case v: Double        => i.putExtra(k, v)
          case v: Float         => i.putExtra(k, v)
          case v: Boolean       => i.putExtra(k, v)
          case v: Serializable  => i.putExtra(k, v)
          case _                => throw new IllegalArgumentException("Unsupported type")
        }
      }
      i
    }
  }
}

trait ResImplicits {
  implicit class ResConversion(res: Int) {
    def r2str(implicit ctx: Context): String = ctx.getResources.getString(res)
    def r2anim(implicit ctx: Context): Animation = AnimationUtils.loadAnimation(ctx, res)
    def r2animator(implicit ctx: Context): Animator = AnimatorInflater.loadAnimator(ctx, res)
    def r2Trans(implicit ctx: Context): Transition = TransitionInflater.from(ctx).inflateTransition(res)
  }
}

trait UnitImplicits {
  implicit class UnitConversion[T](value: T) {
    def dipToPixels(implicit ctx: Context, num: Numeric[T]): Float = {
      val metrics = ctx.getResources.getDisplayMetrics
      TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, num.toFloat(value), metrics)
    }
  }
}

trait ImageImplicits {
  implicit class BytesConversion(bytes: Array[Byte]) {
    def toDrawable(implicit ctx: Context): Drawable = new BitmapDrawable(ctx.getResources, BitmapFactory.decodeByteArray(bytes, 0, bytes.length))
    def toBitmap(implicit ctx: Context): Bitmap = BitmapFactory.decodeByteArray(bytes , 0, bytes.length)
  }

  implicit class BitmapConversion(bitmap: Bitmap) {
    def toDrawable(implicit ctx: Context): Drawable = new BitmapDrawable(ctx.getResources, bitmap)
    def toBytes: Array[Byte] = {
      val stream = new ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
      stream.toByteArray
    }
  }
}

trait ViewImplicits {
  implicit class RichView(v: View) {
    def centerX: Int = (v.getRight - v.getLeft) / 2
    def centerY: Int = (v.getBottom- v.getTop) / 2
    def setVisible(): View = { v.setVisibility(View.VISIBLE); v }
    def setInvisible(): View = { v.setVisibility(View.INVISIBLE); v }
  }
}

trait ViewAnimatorImplicits {
  implicit def func2UpdateListener[V <: View](f: (V, Float) => Unit): Update[V] = (view: V, value: Float) => f(view, value)
}

trait TransitionImplicits {
  implicit class RichTransition(transition: Transition) {
    type OnTransition = (Transition, TransitionListener) => Unit
    private var onTransStarts: Seq[OnTransition] = Seq.empty
    private var onTransEnds: Seq[OnTransition] = Seq.empty

    transition.addListener(new TransitionListener {
      override def onTransitionStart(transition: Transition): Unit = onTransStarts.foreach(_(transition, this))
      override def onTransitionCancel(transition: Transition): Unit = {}
      override def onTransitionEnd(transition: Transition): Unit = onTransEnds.foreach(_(transition, this))
      override def onTransitionPause(transition: Transition): Unit = {}
      override def onTransitionResume(transition: Transition): Unit = {}
    })

    def onTransitionStart(f: OnTransition): Transition = { onTransStarts = onTransStarts :+ f; transition }
    def onTransitionEnd(f: OnTransition): Transition = { onTransEnds = onTransEnds :+ f; transition }
  }
}

trait AnimatorImplicits {
  implicit class RichAnimator(animator: Animator) {
    private var onAnimStarts: Seq[Animator => Unit] = Seq.empty
    private var onAnimEnds  : Seq[Animator => Unit] = Seq.empty

    animator.addListener(new AnimatorListener {
      override def onAnimationEnd(animation: Animator): Unit = onAnimEnds.foreach(_(animator))
      override def onAnimationRepeat(animation: Animator): Unit = {}
      override def onAnimationStart(animation: Animator): Unit = onAnimStarts.foreach(_(animation))
      override def onAnimationCancel(animation: Animator): Unit = {}
    })

    def interpolator(value: TimeInterpolator): Animator = { animator.setInterpolator(value); animator }
    def onAnimationStart(f: Animator => Unit): Animator = { onAnimStarts = onAnimStarts :+ f; animator }
    def onAnimationEnd(f: Animator => Unit): Animator = { onAnimEnds = onAnimEnds :+ f; animator }

    def target(view: View): Animator = { animator.setTarget(view); animator }
  }
}

trait AttrImplicits {
  implicit class AttrConversion(res: (Array[Int], Int)) {
    def r2PxSize(attrSet: AttributeSet, defVal: Int = 0)(implicit ctx: Context): Int = res match {
      case (attrs, index) =>
        val typedArray = ctx.obtainStyledAttributes(attrSet, attrs)
        val pxSize = typedArray.getDimensionPixelSize(index, defVal)
        typedArray.recycle()
        pxSize
    }
  }
}

trait MorphingBtnImplicits {
  implicit class LinearProgressBtnConversion(btn: LinearProgressButton) {
    def morphToNormalBtn(txt: String, colorRes: Int = R.color.accent)(implicit ctx: Context): Unit = {
      val normalBtn = MorphingButton.Params.create()
        .duration(1)
        .cornerRadius(2.dip)
        .width(100.dip)
        .height(56.dip)
        .color(colorRes.r2Color)
        .colorPressed(R.color.md_orange_700.r2Color)
        .text(txt)

      btn.morph(normalBtn)
    }

    def morphToSuccessBtn()(implicit ctx: Context): Unit = {
      val successBtn = MorphingButton.Params.create()
        .duration(1000)
        .cornerRadius(56.dip)
        .width(56.dip)
        .height(56.dip)
        .color(R.color.md_light_green_500.r2Color)
        .icon(R.drawable.ic_done)

      btn.morph(successBtn)
    }

    def morphToErrorBtn()(implicit ctx: Context): Unit = {
      val errorBtn = MorphingButton.Params.create()
        .duration(1000)
        .cornerRadius(56.dip)
        .width(56.dip)
        .height(56.dip)
        .color(R.color.md_red_500.r2Color)
        .icon(R.drawable.ic_close_white_24dp)

      btn.morph(errorBtn)
    }
  }
}