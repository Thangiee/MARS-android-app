object ProguardSettings {
  val options = Seq(
    "-dontobfuscate",
    "-dontoptimize",
    "-keepattributes Signature",
    "-dontpreverify",
    "-dontnote **",
    "-dontwarn org.scaloid.**",
    "-dontwarn org.scalactic.**",
    "-dontwarn com.makeramen.**",
    "-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry",
    "-dontwarn javax.xml.bind.DatatypeConverter",
    "-dontwarn ch.qos.logback.core.net.*",
    "-dontwarn org.slf4j.**",
    "-dontwarn java.**",
    "-dontwarn sun.misc.Unsafe",
    "-keep public class * implements com.bumptech.glide.module.GlideModule",
    "-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {\n    **[] $VALUES;\n    public *;\n}",
    "-keep class com.noveogroup.android.log.LoggerManager { *; }",
    "-keep class com.google.zxing.** { *; }",
    "-keep class com.uta.mars.app.** { *; }",
    "-keep class com.dlazaro66.qrcodereaderview.** { *; }",
    "-keepclassmembers public class * extends com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder {public <init>(...);}"
  )

  val cache = Seq(
    "org",
    "org.scaloid",
    "org.joda",
    "it.gmariotti",
    "scala.collection",
    "android",
    "com.google",
    "com.google.zxing",
    "com.nineoldandroids",
    "com.github",
    "com.github.adnansm.timelytextview",
    "com.typesafe",
    "com.fasterxml.jackson",
    "com.noveogroup",
    "com.dlazaro66.qrcodereaderview",
    "play",
    "scalacache"
  )
}