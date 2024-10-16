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

-keep public class com.trueedu.project.App { *; }
-keep public class com.trueedu.project.di.* { *; }
-keep public class com.trueedu.project.model.* { *; }

-keep class hilt_aggregated_deps.** { *; }

-keepattributes Signature,InnerClasses
-keepattributes SourceFile,LineNumberTable
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes *Annotation*
-keepattributes Annotation

-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

-keep class com.google.**
-dontwarn com.google.**

-keep class com.facebook.**
-dontwarn com.facebook.**

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# okhttp
-dontwarn okhttp2.**
-dontwarn okio.**

#--------------- begin : retrofit ----------
-dontwarn okio.**
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
# Retain Request Body
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
#end

# Amplitude
-keep class com.google.android.gms.ads.** { *; }
-dontwarn okio.**

