scalaVersion := "2.11.7"
scalacOptions += "-Xexperimental"

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

proguardOptions in Android ++= ProguardSettings.options
proguardCache in Android ++= ProguardSettings.cache

// android support libs
val androidSupportV = "23.0.1"
libraryDependencies ++= Seq(
  aar("com.android.support" % "appcompat-v7" % androidSupportV) ,
  aar("com.android.support" % "cardview-v7" % androidSupportV),
  aar("com.android.support" % "recyclerview-v7" % androidSupportV)
)

libraryDependencies ++= Seq(
  "com.github.asyl.animation" % "arcanimator" % "1.0.0",
  "com.pnikosis" % "materialish-progress" % "1.7",
  "org.scaloid" %% "scaloid" % "4.0"
)

// https://github.com/Clans/FloatingActionButton
libraryDependencies += aar("com.github.clans" % "fab" % "1.6.1")
libraryDependencies +=  aar("com.makeramen" % "roundedimageview" % "2.2.1")

// https://github.com/gabrielemariotti/cardslib
libraryDependencies ++= Seq(
  "com.github.gabrielemariotti.cards" % "cardslib-core" % "2.1.0",
  "com.github.gabrielemariotti.cards" % "cardslib-cards" % "2.1.0"
)


run <<= run in Android
install <<= install in Android