import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.barangay.pantal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.barangay.pantal"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read from local.properties or fallback to gradle.properties/environment
        val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: project.findProperty("SUPABASE_URL") ?: ""
        val supabaseKey = localProperties.getProperty("SUPABASE_KEY") ?: project.findProperty("SUPABASE_KEY") ?: ""
        val weatherKey = localProperties.getProperty("OPENWEATHER_API_KEY") ?: project.findProperty("OPENWEATHER_API_KEY") ?: ""

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$weatherKey\"")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        viewBinding = true
        buildConfig = true
    }
}

tasks.register("testClasses") {
    dependsOn("testDebugUnitTest")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    
    // Supabase
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.0")
    
    // Ktor (Switching to OkHttp for WebSocket support)
    implementation("io.ktor:ktor-client-okhttp:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
    implementation(libs.picasso)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
