-keep class me.blog.korn123.easydiary.** { *; }

-keep class com.simplemobiletools.** { *; }
-dontwarn com.simplemobiletools.**

-keep class com.werb.** { *; }
-dontwarn com.werb.**

-keep class android.support.v8.renderscript.** { *; }
-dontwarn android.support.v8.renderscript.**

-keepattributes Signature
-keepattributes Annotation
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8
-dontwarn java.lang.management.**
-dontwarn junit.**

-keep class jp.wasabeef.glide.** { *; }

-keep class org.w3c.dom.** { *; }
-keep class org.xml.sax.** { *; }
-keep class org.dom4j.** { *; }
-keep class javax.xml.** { *; }
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class schemasMicrosoftComOfficeExcel.** { *; }
-keep class schemasMicrosoftComOfficeOffice.** { *; }
-keep class schemasMicrosoftComVml.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.xml.sax.**
-dontwarn org.dom4j.**
-dontwarn javax.xml.**
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.**
-dontwarn schemasMicrosoftComOfficeExcel.**
-dontwarn schemasMicrosoftComOfficeOffice.**
-dontwarn schemasMicrosoftComVml.**
