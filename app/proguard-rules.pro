# Add project specific ProGuard rules here.
# Retrofit / OkHttp / Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.nortlinos.wearos.data.model.** { *; }
-keep class com.nortlinos.wearos.data.api.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.google.gson.**

# WorkManager instantiates InputMergers and workers reflectively; R8 full mode
# otherwise strips their no-arg constructors, so every work request fails to start.
-keep class * extends androidx.work.InputMerger { <init>(); }
-keep class androidx.work.OverwritingInputMerger { <init>(); }
-keep class androidx.work.ArrayCreatingInputMerger { <init>(); }
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
