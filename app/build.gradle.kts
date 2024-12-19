plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    
}

android {
    namespace = "com.example.kurs_06_12_2024"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kurs_06_12_2024"
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

    implementation("androidx.appcompat:appcompat:1.2.0")

    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")
    implementation("com.google.guava:guava:31.1-android")

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth:23.1.0")
   // implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-base:18.0.1")

    // Подключение библиотеки для работы с датами
    implementation("com.jakewharton.threetenabp:threetenabp:1.2.1")

    // Material Calendar View и Material Design компоненты
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
    implementation("com.google.android.material:material:1.9.0")

    // AndroidX библиотеки
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.6.0")

    // Библиотека Firebase Firestore
    implementation("com.google.firebase:firebase-firestore:25.1.1")

    // Тестовые библиотеки
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.espresso:espresso-core:3.5.1")
}
