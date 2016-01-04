package com.uta.mars

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.os.Handler
import android.transition.Transition.TransitionListener
import android.transition.{Transition, TransitionInflater}
import android.view.View
import android.view.animation.{Animation, AnimationUtils}
import com.github.nscala_time.time.DurationBuilder
import com.github.nscala_time.time.Imports._

import scala.language.implicitConversions

package object common {

  private val handler = new Handler()
  private val repeatHandler = new Handler()

  def delay(duration: DurationBuilder)(f: => Unit): Unit = {
    handler.postDelayed(() => f, duration.millis)
  }

  def repeat(initDelay: DurationBuilder = 0.second, period: DurationBuilder = 1.second)(f: => Unit): Runnable = {
    lazy val runnable: Runnable = () => {f; repeatHandler.postDelayed(runnable, period.millis)}
    repeatHandler.postDelayed(runnable, initDelay.millis)
    runnable
  }

  def clearAllRepeatTasks(): Unit = repeatHandler.removeCallbacksAndMessages(null)

  implicit class ResConversion(res: Int) {
    def r2str(implicit ctx: Context): String = ctx.getResources.getString(res)
    def r2anim(implicit ctx: Context): Animation = AnimationUtils.loadAnimation(ctx, res)
    def r2Trans(implicit ctx: Context): Transition = TransitionInflater.from(ctx).inflateTransition(res)
  }

  implicit class RichView(v: View) {
    def centerX: Int = (v.getRight - v.getLeft) / 2
    def centerY: Int = (v.getBottom- v.getTop) / 2
  }

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

  implicit class RichAnimator(animator: Animator) {
    private var onAnimStarts: Seq[Animator => Unit] = Seq.empty
    private var onAnimEnds  : Seq[Animator => Unit] = Seq.empty

    animator.addListener(new AnimatorListener {
      override def onAnimationEnd(animation: Animator): Unit = onAnimEnds.foreach(_(animator))
      override def onAnimationRepeat(animation: Animator): Unit = {}
      override def onAnimationStart(animation: Animator): Unit = onAnimStarts.foreach(_(animation))
      override def onAnimationCancel(animation: Animator): Unit = {}
    })

    def onAnimationStart(f: Animator => Unit): Animator = { onAnimStarts = onAnimStarts :+ f; animator }
    def onAnimationEnd(f: Animator => Unit): Animator = { onAnimEnds = onAnimEnds :+ f; animator }
  }
}
