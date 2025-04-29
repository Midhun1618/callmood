// Root-level build.gradle.kts

plugins {
    // This will apply plugins for the whole project
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}