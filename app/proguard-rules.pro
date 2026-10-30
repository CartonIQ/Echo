# Proguard rules for Echo - Spy Mic & Hearing Aid
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.android.ump.** {
   public *;
}
-keep class com.cartoniq.echo.audio.** { *; }
-keep class com.cartoniq.echo.ads.** { *; }
