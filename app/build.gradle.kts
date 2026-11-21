plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.waagent"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.waagent"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1-m0"
    }

    buildTypes {
        // M0 is a debug build only. Debug APKs are signed automatically with a debug key,
        // so you can install them on your own phone without any signing setup.
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// No third-party libraries on purpose. M0 uses only what Android already ships.
