// Top-level build file - FIXED VERSION
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false  // ← FIXED: Added Hilt plugin
}

// ← FIXED: Removed duplicate buildscript block
// All plugin dependencies are now declared in the plugins block above
