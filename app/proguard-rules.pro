-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers,allowobfuscation class * { @com.google.gson.annotations.SerializedName <fields>; }
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Wallpapers are persisted as Gson JSON inside Room, so their fields must survive R8.
-keep class com.wildwalls.app.domain.model.** { *; }
-keep class com.wildwalls.app.data.remote.dto.** { *; }
-keep class com.wildwalls.app.data.repository.FeedResult$* { *; }
