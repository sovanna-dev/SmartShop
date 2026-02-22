// Root build.gradle.kts
// This file registers plugins for the whole project.
// We use "apply false" because we don't apply them here —
// only the app module applies what it needs.

plugins {
    alias(libs.plugins.android.application)     apply false
    alias(libs.plugins.android.library)         apply false
    alias(libs.plugins.kotlin.android)          apply false
    alias(libs.plugins.kotlin.kapt)             apply false
    alias(libs.plugins.hilt)                    apply false
    alias(libs.plugins.google.services)         apply false
    alias(libs.plugins.navigation.safeargs) apply false
}