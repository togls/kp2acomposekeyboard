plugins {
    alias(libs.plugins.android.library)
}

val sdkCompile = providers.gradleProperty("android.compileSdk").get().toInt()
val sdkMin = providers.gradleProperty("android.minSdk").get().toInt()

android {
    namespace = "keepass2android.pluginsdk"
    compileSdk = sdkCompile

    defaultConfig {
        minSdk = sdkMin
    }
}
