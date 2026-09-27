# Add project specific ProGuard rules here.
# Retrofit / OkHttp / Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.nortlinos.wearos.data.model.** { *; }
-keep class com.nortlinos.wearos.data.api.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.google.gson.**
