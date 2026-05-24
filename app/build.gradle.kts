import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val compileSdkVersion = providers.gradleProperty("android.compileSdk").get().toInt()
val targetSdkVersion = providers.gradleProperty("android.targetSdk").get().toInt()
val minSdkVersion = providers.gradleProperty("android.minSdk").get().toInt()

val keystorePropertiesFile = rootProject.file("keystore.properties")

val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(::load)
    }
}

fun signingValue(
    propertyKey: String,
    environmentKey: String,
): String? {
    return keystoreProperties.getProperty(propertyKey)
        ?: providers.environmentVariable(environmentKey).orNull
}

val releaseStoreFile = signingValue(
    propertyKey = "storeFile",
    environmentKey = "ANDROID_KEYSTORE_PATH",
)

val releaseStorePassword = signingValue(
    propertyKey = "storePassword",
    environmentKey = "ANDROID_KEYSTORE_PASSWORD",
)

val releaseKeyAlias = signingValue(
    propertyKey = "keyAlias",
    environmentKey = "ANDROID_KEY_ALIAS",
)

val releaseKeyPassword = signingValue(
    propertyKey = "keyPassword",
    environmentKey = "ANDROID_KEY_PASSWORD",
)

val hasReleaseSigning =
    !releaseStoreFile.isNullOrBlank() &&
            !releaseStorePassword.isNullOrBlank() &&
            !releaseKeyAlias.isNullOrBlank() &&
            !releaseKeyPassword.isNullOrBlank()

android {
    namespace = "io.github.togls.kp2acomposekeyboard"
    compileSdk = compileSdkVersion

    defaultConfig {
        applicationId = "io.github.togls.kp2acomposekeyboard"
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }

        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    jvmToolchain(17)
}

hilt {
    enableAggregatingTask = true
}

dependencies {
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.espresso.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(project(":kp2a-plugin-sdk"))

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}
