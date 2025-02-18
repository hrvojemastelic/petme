plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services") // Apply the Google Services plugin here
}

android {
    namespace = "com.example.petme"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.petme"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.activity)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase platform BOM for consistent versions
    implementation(platform("com.google.firebase:firebase-bom:33.5.0"))

    // Firebase services
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:22.1.0")
    implementation("com.google.firebase:firebase-firestore:24.9.0")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Firebase Cloud Messaging (new dependency)
    implementation (platform("com.google.firebase:firebase-bom:32.2.0")) // Use the latest version
    implementation ("com.google.firebase:firebase-messaging-ktx")
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Zoomage for image zooming
    implementation("com.jsibbold:zoomage:1.3.1")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Material Design Components
    implementation("com.google.android.material:material:1.9.0")
    implementation ("com.google.code.gson:gson:2.8.8")

}
