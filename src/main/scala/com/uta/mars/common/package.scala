package com.uta.mars

import java.util.concurrent.{TimeUnit, ScheduledThreadPoolExecutor}

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.os.{Handler, Looper}
import android.transition.{TransitionInflater, Transition}
import android.transition.Transition.TransitionListener
import android.view.View
import android.view.animation.{AnimationUtils, Animation}

import scala.concurrent.duration._

package object common {

  private val handler = new Handler()
  private val repeatHandler = new Handler()

  def delay(mills: Long)(f: => Unit): Unit = {
    handler.postDelayed(() => f, mills)
  }

  def repeat(initDelay: Duration = 0.second, period: Duration = 1.second)(f: => Unit): Runnable = {
    lazy val runnable: Runnable = () => {f; repeatHandler.postDelayed(runnable, period.toMillis)}
    repeatHandler.postDelayed(runnable, initDelay.toMillis)
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
