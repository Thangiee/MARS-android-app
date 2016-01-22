package com.uta.mars.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.view.{View, Menu, MenuItem}
import com.github.clans.fab.FloatingActionButton
import com.github.florent37.viewanimator.ViewAnimator
import com.github.nscala_time.time.Imports._
import com.uta.mars.R
import com.uta.mars.app.common._
import org.scaloid.common._

class HomeAct extends BaseActivity {

  private lazy val toolbar      = find[Toolbar](R.id.toolbar)
  private lazy val profileFAB   = find[FloatingActionButton](R.id.fab_profile)
  private lazy val clockInFAB   = find[FloatingActionButton](R.id.fab_clock_in)
  private lazy val clockOutFAB  = find[FloatingActionButton](R.id.fab_clock_out)
  private lazy val timeSheetFAB = find[FloatingActionButton](R.id.fab_time_sheet)
  private lazy val stopWatch    = find[StopWatchView](R.id.stop_watch)

  private val CLOCK_IN_REQUEST  = 100
  private val CLOCK_OUT_REQUEST = 101

  protected override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    setContentView(R.layout.screen_home)
    setSupportActionBar(toolbar)
    setTitle("Home")

    Seq(profileFAB, clockInFAB, timeSheetFAB, clockOutFAB).foreach(_.hide(false))

    timeSheetFAB.onClick {
      new AlertDialogBuilder("Time Sheet", s"Do you want your time sheet of the current pay period to be sent to your email?") {
        positiveButton("Yes", onYesBtnClick())
        negativeButton("No")
      }.show()

      def onYesBtnClick(): Unit = {
        MarsApi.emailTimeSheet()
          .map(_ => Snackbar.make(find(R.id.root), "Email sent. I may take up to a few minute to arrive.", 5000).show())
          .badMap(err => showApiErrorDialog(err.code))
      }
    }

    clockInFAB.onClick {
      clockInFAB.setProgress(5, true)
      loadAsstInfo.map(asst => runOnUiThread {
        clockInFAB.setProgress(100, true)
        animateFABProgress(clockInFAB)
        delay(550.millis)(startActivityForResult(ClockInOutAct(asst, isClockingIn=true), CLOCK_IN_REQUEST))
      })
    }

    clockOutFAB.onClick {
      clockOutFAB.setProgress(5, true)
      loadAsstInfo.map(asst => runOnUiThread {
        animateFABProgress(clockOutFAB)
        delay(550.millis)(startActivityForResult(ClockInOutAct(asst, isClockingIn=false), CLOCK_OUT_REQUEST))
      })
    }

    profileFAB.onClick {
      // While showing load animations, cache the assistant info beforehand
      // for the Profile activity to show it almost instantly.
      profileFAB.setProgress(5, true)
      loadAsstInfo.map(_ => runOnUiThread {
        animateFABProgress(profileFAB)
        delay(550.millis)(startActivity[ProfileAct])
      })
    }

    def animateFABProgress(fab: FloatingActionButton): Unit = {
      ViewAnimator.animate(fab)
        .custom((fab: FloatingActionButton, value: Float) => fab.setProgress(value.toInt, false), 5, 100)
        .accelerate()
        .duration(500)
        .start()
    }
  }

  override def onResume(): Unit = {
    super.onResume()
    setUpViews()
  }

  override def onPause(): Unit = {
    super.onPause()
    Seq(profileFAB, clockInFAB, timeSheetFAB, clockOutFAB).foreach(fab => {
      fab.hideProgress()
      fab.setProgress(0, false)
      fab.hide(false)
    })
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    (requestCode, resultCode) match {
      case (CLOCK_IN_REQUEST, Activity.RESULT_OK) =>
        stopWatch.reset()
        stopWatch.start()
        clockInFAB.hide(true)
        delay(250.millis)(clockOutFAB.show(true))

      case (CLOCK_OUT_REQUEST, Activity.RESULT_OK) =>
        stopWatch.stop()
        clockOutFAB.hide(true)
        delay(250.millis)(clockInFAB.show(true))

      case _ =>
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.overflow, menu)
    true
  }

  override def onBackPressed(): Unit = {
    // Make it so the user can come back to this screen in the current state after they press
    // the back button to go to the android home screen. Without this code, the login screen
    // will be launched instead.
    val androidHomeScreen = new Intent(Intent.ACTION_MAIN)
    androidHomeScreen.addCategory(Intent.CATEGORY_HOME)
    androidHomeScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(androidHomeScreen)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.menu_logout => finish(); startActivity[LoginAct]; true
      case _ /* no match */ => super.onOptionsItemSelected(item)
    }
  }

  private def setUpViews(): Unit = {
    MarsApi.recordsFromThisPayPeriod()
      .badMap(err => showApiErrorDialog(err.code, "Retry", _ => setUpViews()))
      .map {
        case Records(Nil)         => setupClockIn() // no previous records so clock in
        case Records(record :: _) =>
          // defined means that user has previously clocked out
          if (record.outTime.isDefined) setupClockIn() else setupClockOut(record)
      }

    def setupClockIn(): Unit = runOnUiThread {
      // Make those FABs pop in from a left to right sequence
      Seq(profileFAB, clockInFAB, timeSheetFAB).zip(1 to 3).foreach {
        case (fab, i) => delay((i*250).millis)(fab.show(true))
      }
    }

    def setupClockOut(record: Record): Unit = runOnUiThread {
      // Make those FABs pop in from a left to right sequence
      Seq(profileFAB, clockOutFAB, timeSheetFAB).zip(1 to 3).foreach {
        case (fab, i) => delay((i*250).millis)(fab.show(true))
      }
      // todo: show how long the asst has been clocked in based on record clock in time
    }
  }

  private def loadAsstInfo: FutureOr[Assistant, Unit] = {
    MarsApi.assistantInfo()
      .badMap {
        case Err(403, _)  => runOnUiThread(profileFAB.hideProgress()); showReLoginDialog()
        case Err(code, _) => runOnUiThread(profileFAB.hideProgress()); showApiErrorDialog(code)
      }
  }
}
