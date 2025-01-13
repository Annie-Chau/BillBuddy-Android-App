plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.learning.billbuddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.learning.billbuddy"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(fileTree(mapOf(
        "dir" to "src/lib",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.auth)

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:23.1.0")

    // Google Sign-In
    implementation(libs.play.services.auth.v2050)

    // Add the Glide dependency so that the uploaded images can be displayed as the wanted shape
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation ("com.android.support:cardview-v7:26.0.0")

    // Zalo
    implementation(libs.okhttp)
    implementation(libs.commons.codec)

    implementation ("com.stripe:stripe-android:21.3.1")


}