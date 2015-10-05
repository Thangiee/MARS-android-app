object ProguardSettings {
  val options = Seq(
    "-dontobfuscate",
    "-dontoptimize",
    "-keepattributes Signature",
    "-dontpreverify",
    "-dontwarn org.scaloid.**"
  )

  val cache = Seq(
    "org.scaloid",
    "org.joda",
    "it.gmariotti",
    "scala.collection"
  )
}