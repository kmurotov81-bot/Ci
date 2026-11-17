// Root build.gradle.kts — bu yerda hech qanday versiya raqami yozilmaydi,
// barchasi gradle/libs.versions.toml dan olinadi (single source of truth).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
