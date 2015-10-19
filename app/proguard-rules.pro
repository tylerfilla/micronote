
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

# Appodeal stuff

-keep class com.appodeal.** { *; }
-keep class com.applovin.** { *; }
-keep class com.mopub.** { *; }
-keep class org.nexage.** { *; }
-keep class com.chartboost.** { *; }
-dontwarn com.chartboost.**
-keep class com.amazon.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.GooglePlayServicesUtil { *; }
-keep class ru.mail.android.mytarget.** { *; }
-keep class com.unity3d.ads.** { *; }
-keep class com.applifier.** { *; }
-keep class android.support.v4.** { *; }

-keep class * extends java.util.ListResourceBundle {
protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
@com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}

-dontwarn com.facebook.ads.**
-dontwarn com.jirbo.adcolony.**
-dontwarn com.vungle.**
-dontwarn com.google.**
-dontwarn com.appodeal.ads.video.**
-dontwarn android.support.design.widget.**
-dontwarn com.mopub.**
-dontwarn com.appodeal.ads.networks.**

-keep class * extends io.microdev.note.activity.ActivityList$ActionModeCallback { *; }
