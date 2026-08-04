plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.blueray.marasy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.blueray.marasy"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.3"

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
    viewBinding {
        viewBinding.enable = true
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // Retrofit and OkHttp
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.7")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    //view model
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")

    //toasty
    implementation ("com.github.GrenderG:Toasty:1.5.2")

    //otp view
    implementation ("com.github.appsfeature:otp-view:1.1")

    //navigation
    implementation ("androidx.navigation:navigation-fragment-ktx:2.9.2")
    implementation ("androidx.navigation:navigation-ui-ktx:2.9.2")

    //bottom navigation bar
    implementation("com.github.ibrahimsn98:NiceBottomBar:2.2")


    //image slider
    implementation("com.github.denzcoskun:ImageSlideshow:0.1.2")

    //swipe to refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    implementation("com.airbnb.android:lottie:5.2.0")
    implementation("ru.tinkoff.scrollingpagerindicator:scrollingpagerindicator:1.2.5")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")

    // OneSignal Push Notifications
    implementation("com.onesignal:OneSignal:[4.0.0, 4.99.99]")
}