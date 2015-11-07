object ProguardSettings {
  val options = Seq(
    "-dontobfuscate",
    "-dontoptimize",
    "-keepattributes Signature",
    "-dontpreverify",
    "-dontwarn org.scaloid.**",
    "-dontwarn com.makeramen.**",
    "-keep class io.codetail.animation.arcanimator.** { *; }"
  )

  val cache = Seq(
    "org.scaloid",
    "org.joda",
    "it.gmariotti",
    "scala.collection",
    "android",
    "com.google",
    "com.nineoldandroids",
    "com.github"
  )
}