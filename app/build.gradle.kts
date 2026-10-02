plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val inputVersion = (project.findProperty("versionName") as? String)
    ?: System.getenv("APP_VERSION")
    ?: "1.0.0"

val cleanVersionName = inputVersion.removePrefix("v")
val inputVersionCode = (project.findProperty("versionCode") as? String)?.toIntOrNull()
    ?: System.getenv("APP_VERSION_CODE")?.toIntOrNull()
    ?: 1

android {
    namespace = "com.dyno.navi_noti"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    signingConfigs {
        create("release") {
            val customKeystorePath = System.getenv("KEYSTORE_FILE")
            val defaultRootKeystore = rootProject.file("navi-noti-release.jks")
            val keystoreFile = when {
                !customKeystorePath.isNullOrBlank() -> file(customKeystorePath)
                defaultRootKeystore.exists() -> defaultRootKeystore
                else -> file("navi-noti-release.jks")
            }

            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "navinoti2026"
                keyAlias = System.getenv("KEY_ALIAS") ?: "navi_noti_key"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "navinoti2026"
            }
        }
    }

    defaultConfig {
        applicationId = "com.dyno.navi_noti"
        minSdk = 28
        targetSdk = 36
        versionCode = inputVersionCode
        versionName = cleanVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}