# ============================================================
# PureLink ProGuard Rules
# ============================================================

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.Continuation$Companion {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# --- Kotlin Serialization (if used in future) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# --- Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- AndroidX Lifecycle / ViewModel ---
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keepclassmembers class * {
    @androidx.lifecycle.ViewModel$OnViewModelCleared <methods>;
}
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepattributes RuntimeVisibleAnnotations

# --- Keep all data classes used in serialization or state flows ---
-keepclassmembers class com.ahmedsamy.purelink.** {
    <fields>;
}

# --- Accessibility Service ---
-keep class com.ahmedsamy.purelink.ClipboardService { *; }

# --- Tile Service ---
-keep class com.ahmedsamy.purelink.PureLinkTileService { *; }

# --- Share Activity ---
-keep class com.ahmedsamy.purelink.ShareActivity { *; }

# --- OkHttp / Networking (if added in future) ---
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- java.net.URL and HttpURLConnection ---
-keep class java.net.URL { *; }
-keep class java.net.HttpURLConnection { *; }

# --- Preserve source file names and line numbers for crash reports ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Remove logging in release ---
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
