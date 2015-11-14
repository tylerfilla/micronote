
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-dontwarn com.google.android.gms.**
-keep public class com.google.android.gms.**

# Appodeal stuff

-keep class com.appodeal.** { *; }
-keep class com.amazon.** { *; }
-keep class com.mopub.** { *; }
-keep class org.nexage.** { *; }
-keep class com.applovin.** { *; }
-keep class com.chartboost.** { *; }
-keep class com.unity3d.ads.** { *; }
-keep class com.applifier.** { *; }
-keep class com.yandex.** { *; }
-keep class com.inmobi.** { *; }
-keep class ru.mail.android.mytarget.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.common.GooglePlayServicesUtil { *; }
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
-dontwarn com.startapp.**
-dontwarn com.yandex.**
-dontwarn com.inmobi.**
-dontwarn com.appodeal.ads.utils.**
-keep class android.support.v4.app.Fragment { *; }
-keep class android.support.v4.app.FragmentActivity { *; }
-keep class android.support.v4.app.FragmentManager { *; }
-keep class android.support.v4.app.FragmentTransaction { *; }
-keep class android.support.v4.content.LocalBroadcastManager { *; }
-keep class android.support.v4.util.LruCache { *; }
-keep class android.support.v4.view.PagerAdapter { *; }
-keep class android.support.v4.view.ViewPager { *; }

-keep public class com.tappx.** { *; }

-dontwarn com.chartboost.**
-dontwarn com.mopub.**

-keep class * extends io.microdev.note.activity.ActivityList$ActionModeCallback { *; }
