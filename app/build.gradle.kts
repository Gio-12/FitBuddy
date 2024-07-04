plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
//    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fitbuddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitbuddy"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Kotlin Standard Library
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.work.runtime.ktx)

    //DAGGER
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // ROOM
    val room = "2.6.1"

    kapt("androidx.room:room-compiler:$room")
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.rxjava2)
    implementation(libs.androidx.room.rxjava3)
    implementation(libs.androidx.room.guava)
    testImplementation(libs.androidx.room.testing)
    implementation(libs.androidx.room.paging)

    // GMS GOOGLE
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.androidx.localbroadcastmanager)

    // Import the Firebase BoM
//    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
//    implementation("com.google.firebase:firebase-analytics")
//    implementation("com.google.firebase:firebase-firestore")
//    implementation("com.google.firebase:firebase-database-ktx:20.3.0")

//    // CHARTS
    implementation(libs.mpandroidchart)

    //STEPS
    implementation(platform(libs.androidx.compose.bom.v20231001))
    implementation(libs.activity.compose)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material)

    //GLIDE GIF
    implementation(libs.glide)
    annotationProcessor (libs.compiler)
}

//apply(plugin = "com.google.gms.google-services")