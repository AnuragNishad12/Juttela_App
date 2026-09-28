plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.juttela"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anurag.juttela"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        resValue("string", "mapbox_access_token", project.findProperty("MAPBOX_ACCESS_TOKEN") as String)
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
    implementation(libs.androidx.navigation.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform( libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.onesignal:OneSignal:5.9.1")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.revenuecat.purchases:purchases:10.17.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")
    implementation("com.mapbox.maps:android-ndk27:11.29.0")
    implementation("com.mapbox.extension:maps-compose-ndk27:11.29.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("com.google.android.gms:play-services-auth:21.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")


}