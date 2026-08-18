# --- App Services (Keep Android System Entry Points) ---
-keep class com.ahmedsamy.purelink.ClipboardService { *; }
-keep class com.ahmedsamy.purelink.PureLinkTileService { *; }

# --- Preserve crash report info (Keep lines for readable stack traces) ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Strip logging in release (Correct & Active Implementation) ---
-dontoptimize
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}