plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.googleService)
    id("org.jetbrains.kotlin.kapt") // Esto es necesario
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.arki_deportes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.arki_deportes"
        minSdk = 26
        targetSdk = 34
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.base)

    // Firebase
    implementation(platform(libs.firebaseBom))
    implementation(libs.bundles.firebase.base)


    // Navigation & UI
    implementation(libs.materialIconsExtended)
    implementation("androidx.navigation:navigation-compose:2.7.6")

    //EXOPLAYER -REPRODUCTOR STREAMING
    implementation(libs.bundles.exoplayer.base)
    implementation(libs.exoplayernotification)
    implementation("androidx.media3:media3-transformer:${libs.versions.exoplayer.get()}")
    implementation("androidx.media3:media3-common:${libs.versions.exoplayer.get()}")
    implementation(libs.exoplayer.core)


    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    implementation(libs.accompanist.swiperefresh)


    //# Google Play Services Location
    implementation(libs.playServiceslocation)

    //#Dependencia de Coroutines
    implementation(libs.kotlinxcoroutinescore)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //#Logs con Timber
    implementation(libs.timber)

    //#Optimización de Imágenes
    implementation(libs.coilcompose)


    //#tareas en segundo plano (como sincronización o envío de datos)
    implementation(libs.workManagerktx)

    //#inyección de dependencias para un código más modular y limpio
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation(libs.accompanist.webview)

    implementation(libs.jsoup)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


}