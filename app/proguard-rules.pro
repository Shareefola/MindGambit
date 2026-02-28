# ============================================================
# MindGambit — ProGuard Rules
# ============================================================

# Keep application class
-keep class com.mindgambit.app.MindGambitApp { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin Serialization (if added later)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Lottie
-keep class com.airbnb.lottie.** { *; }

# Stockfish JNI bridge
-keep class com.mindgambit.app.data.engine.StockfishEngine { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# Firebase
-keep class com.google.firebase.** { *; }

# Crash reporting — keep line numbers
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
