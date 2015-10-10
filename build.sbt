import android.Keys._

scalaVersion := "2.11.7"

packagingOptions in Android := PackagingOptions(
  excludes =
    "META-INF/notice.txt" ::
      "META-INF/license.txt" ::
      "META-INF/LICENSE" ::
      "META-INF/NOTICE" ::
      "META-INF/LICENSE.txt" ::
      "META-INF/NOTICE.txt" ::
      Nil
)

proguardOptions in Android ++= Seq(
  "-dontobfuscate",
  "-dontoptimize",
  "-keepattributes Signature",
  "-dontpreverify"
)

// android support libs
val androidSupportV = "23.0.1"
libraryDependencies ++= Seq(
  aar("com.android.support" % "appcompat-v7" % androidSupportV)
)

run <<= run in Android

install <<= install in Android