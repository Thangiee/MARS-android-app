object ProguardSettings {
  val options = Seq(
    "-dontobfuscate",
    "-dontoptimize",
    "-keepattributes Signature",
    "-dontpreverify",
    "-dontnote **",
    "-dontwarn org.scaloid.**",
    "-dontwarn com.makeramen.**",
    "-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry",
    "-dontwarn javax.xml.bind.DatatypeConverter",
    "-dontwarn ch.qos.logback.core.net.*",
    "-dontwarn org.slf4j.**",
    "-dontwarn java.**",
    "-dontwarn sun.misc.Unsafe",
    "-keepclassmembers public class * extends com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder {public <init>(...);}"
  )

  val cache = Seq(
    "org.scaloid",
    "org.joda",
    "it.gmariotti",
    "scala.collection",
    "android",
    "com.google",
    "com.nineoldandroids",
    "com.github",
    "com/github/adnansm/timelytextview",
    "com.typesafe"
  )
}