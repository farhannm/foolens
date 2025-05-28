plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.proyek.foolens"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.proyek.foolens"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
    implementation(libs.androidx.room.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("androidx.paging:paging-compose:3.3.0")
    implementation ("androidx.paging:paging-runtime-ktx:3.3.0")

    implementation ("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")
    implementation ("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.compose.foundation:foundation:1.5.0")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.paging:paging-compose:3.3.0")
    implementation("androidx.paging:paging-runtime-ktx:3.3.0")

    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")
    implementation(libs.androidx.runtime.livedata)

    implementation ("com.jakewharton.timber:timber:5.0.1")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

    // Material Design 3
    implementation ("com.google.android.material:material:1.12.0")

    // Material 3 untuk Compose
    implementation ("androidx.compose.material3:material3:1.3.1")

    //Splash
    implementation ("androidx.core:core-splashscreen:1.0.1")

    //Cam
    implementation ("androidx.camera:camera-core:1.3.0")

    // CameraX dependencies
    implementation ("androidx.camera:camera-camera2:1.3.1")
    implementation ("androidx.camera:camera-lifecycle:1.3.1")
    implementation ("androidx.camera:camera-view:1.3.1")

    // Activity dependencies for result contracts
    implementation ("androidx.activity:activity-compose:1.8.1")

    implementation("com.google.accompanist:accompanist-permissions:0.30.1")

    // ML Kit dependencies for OCR
    implementation ("com.google.mlkit:text-recognition:16.0.0")

    //Swipe refresh
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.27.0")

    //Coil Images
    implementation("io.coil-kt.coil3:coil:3.1.0")
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

    //ML Kit : Barcode
    implementation ("com.google.mlkit:barcode-scanning:17.3.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.2")
    implementation ("com.google.android.gms:play-services-safetynet:18.0.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}