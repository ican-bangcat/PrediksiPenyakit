plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.prediksipenyakit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.prediksipenyakit"
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

    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.23.2")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //Untuk Gambar Artikel
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Coil untuk load gambar
    implementation("io.coil-kt:coil:2.6.0")
    // Material Design (untuk CoordinatorLayout & CollapsingToolbar)
    implementation("com.google.android.material:material:1.11.0")
    // CardView untuk card layout
    implementation("androidx.cardview:cardview:1.0.0")
    // Optional: Untuk API calls (jika nanti perlu)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Coroutines untuk async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // --- SUPABASE & KTOR ---
    // BOM untuk mengatur versi otomatis
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))

    // Modul Supabase yang kita butuh
    implementation("io.github.jan-tennert.supabase:postgrest-kt") // Database
    implementation("io.github.jan-tennert.supabase:auth-kt")    // Auth (Login/Register)
    implementation("io.github.jan-tennert.supabase:storage-kt")   // Storage (buat gambar artikel nanti)

    // Engine Ktor (Wajib buat Supabase jalan di Android)
    implementation("io.ktor:ktor-client-android:3.0.0")

    // Serialization (Buat ubah Data JSON <-> Kotlin Object)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Coil (Untuk load gambar)
    implementation("io.coil-kt:coil:2.6.0")
}