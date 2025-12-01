plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "dev.afgk.localsound"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.afgk.localsound"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)

    // Navigation Dependencies

    // Support for Views/Fragments
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Support for feature module support for fragments
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)

    // Room dependecies

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Support for Kotlin features

    // Symbol processing
    ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}