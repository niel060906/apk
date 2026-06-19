# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ========== MEDIA3/EXOPLAYER RULES ==========
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-keep class androidx.media3.session.** { *; }
-dontwarn androidx.media3.**

# ========== ROOM DATABASE RULES ==========
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# ========== FIREBASE & FIRESTORE RULES ==========
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep @com.google.firebase.** class * { *; }
-keep class com.google.common.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.**

# ========== MOSHI RULES ==========
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.Json class * { *; }
-keepclassmembers class * { @com.squareup.moshi.* <methods>; }
-dontwarn com.squareup.moshi.**

# ========== KOTLIN/COROUTINES RULES ==========
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# ========== COMPOSE RULES ==========
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ========== GENERAL RULES ==========
-keepattributes Signature,RuntimeVisibleAnnotations
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep public class * extends android.app.Service
