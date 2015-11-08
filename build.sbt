scalaVersion := "2.11.7"
scalacOptions += "-Xexperimental"

packagingOptions := PackagingOptions(
  excludes =
    "META-INF/notice.txt" ::
      "META-INF/license.txt" ::
      "META-INF/LICENSE" ::
      "META-INF/NOTICE" ::
      "META-INF/LICENSE.txt" ::
      "META-INF/NOTICE.txt" ::
      Nil
)

proguardOptions ++= ProguardSettings.options
proguardCache ++= ProguardSettings.cache

resolvers += "jitpack" at "https://jitpack.io"

// android support libs
val androidSupportV = "23.0.1"
libraryDependencies ++= Seq(
  aar("com.android.support" % "appcompat-v7" % androidSupportV),
  aar("com.android.support" % "cardview-v7" % androidSupportV),
  aar("com.android.support" % "recyclerview-v7" % androidSupportV)
)

libraryDependencies ++= Seq(
  "org.scaloid"              %% "scaloid"              % "4.0",   // https://github.com/pocorall/scaloid
  "com.github.asyl.animation" % "arcanimator"          % "1.0.0", // https://github.com/asyl/ArcAnimator
  "com.pnikosis"              % "materialish-progress" % "1.7",   // https://github.com/pnikosis/materialish-progress
  "com.github.clans"          % "fab"                  % "1.6.1", // https://github.com/Clans/FloatingActionButton
  "com.makeramen"             % "roundedimageview"     % "2.2.1", // https://github.com/vinc3m1/RoundedImageView
  "com.rengwuxian.materialedittext" % "library"        % "2.1.4", // https://github.com/rengwuxian/MaterialEditText

  "com.github.dmytrodanylyk"  % "android-morphing-button" % "98a4986e56", // https://github.com/dmytrodanylyk/android-morphing-button

  // https://github.com/gabrielemariotti/cardslib
  "com.github.gabrielemariotti.cards" % "cardslib-core" % "2.1.0",
  "com.github.gabrielemariotti.cards" % "cardslib-cards" % "2.1.0"
)

run <<= run in Android
install <<= install in Android