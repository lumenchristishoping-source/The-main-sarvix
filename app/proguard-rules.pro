# Sarvix ProGuard Rules - FIXED

# Firebase Models - CRITICAL!
-keep class com.sarvix.app.data.model.** { *; }
-keepclassmembers class com.sarvix.app.data.model.** { *; }
-keepattributes *Annotation*
-keepattributes Signature

# Hilt / Dagger
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Coil
-keep class coil.** { *; }

# ExoPlayer / Media3
-keep class androidx.media3.** { *; }

# ML Kit Translation
-keep class com.google.mlkit.** { *; }

# ViewModels & Repositories
-keep class com.sarvix.app.viewmodel.** { *; }
-keep class com.sarvix.app.data.repository.** { *; }
