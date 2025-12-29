#noinspection ShrinkerUnresolvedReference
-keep class me.blog.korn123.easydiary.** { *; }
-dontwarn me.blog.korn123.easydiary.**

-keep class com.simplemobiletools.** { *; }
-dontwarn com.simplemobiletools.**

-keep class com.werb.** { *; }
-dontwarn com.werb.**

-keep class androidx.renderscript.** { *; }
-dontwarn androidx.renderscript.**

-keep class com.google.common.** { *; }
-dontwarn com.google.common.**

-keep class com.google.api.** { *; }
-dontwarn com.google.api.**

-keep class org.apache.commons.** { *; }
-dontwarn org.apache.commons.**

-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# For BannerViewPager
-keep class androidx.recyclerview.widget.**{*;}
-keep class androidx.viewpager2.widget.**{*;}

-keepattributes Signature
-keepattributes Annotation
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn java.lang.management.**
-dontwarn junit.**

-keep class jp.wasabeef.glide.** { *; }

-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.imageio.**

-dontwarn kotlinx.coroutines.flow.**inlined**

-dontwarn com.ibm.icu.**
-dontwarn org.jasypt.web.**

-dontwarn com.caverock.androidsvg.SVG
-dontwarn com.caverock.androidsvg.SVGParseException
-dontwarn pl.droidsonroids.gif.GifDrawable

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep generic signatures for Gson TypeToken
-keepattributes Signature

# Keep Gson TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep your data classes used with Gson (replace with your actual package)
-keep class me.blog.korn123.easydiary.models.** { *; }
-keep class your.package.name.data.** { *; }