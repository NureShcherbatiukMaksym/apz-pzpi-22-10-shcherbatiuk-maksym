plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.soilscout"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.soilscout"
        minSdk = 24
        targetSdk = 35
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.okhttp)
    implementation(libs.material)

    implementation("com.google.android.gms:play-services-auth:20.0.1")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation("io.socket:socket.io-client:2.0.0")
    implementation ("org.json:json:20231013")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.google.android.material:material:1.10.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation ("androidx.room:room-runtime:2.6.1")
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("com.google.android.material:material:1.11.0")

    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    // Firebase BOM (Bill of Materials) - керує версіями Firebase бібліотек
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Перевір останню версію BOM

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-auth:22.3.1")

    // Google Sign-In (вже є, але переконайся, що версія сумісна)
    implementation("com.google.android.gms:play-services-auth:20.0.1") // Можеш оновити до 21.1.0 або новішої, якщо використовуєш новішу BOM


}