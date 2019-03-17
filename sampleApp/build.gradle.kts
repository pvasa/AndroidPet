// Copyright 2019 Priyank Vasa
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("com.google.gms.google-services")
}

android {

    signingConfigs {

        create("release") {
            storeFile = file("$rootDir/keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "android-release"
            keyPassword = System.getenv("KEYALIAS_PASSWORD")
        }

        create("stage") {
            storeFile = file("$rootDir/keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "android-stage"
            keyPassword = System.getenv("KEYALIAS_STAGE_PASSWORD")
        }
    }

    compileSdkVersion(Config.Android.sdk)

    defaultConfig {
        applicationId = "com.priyankvasa.android.cameraviewex"
        minSdkVersion(Config.Android.minSdk)
        targetSdkVersion(Config.Android.sdk)
        versionCode = Config.versionCode
        versionName = Config.versionName
        renderscriptTargetApi = 21
    }

    buildTypes {

        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        getByName("release") {
            isDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["release"]
        }

        create("stage") {
            initWith(buildTypes["release"])
            applicationIdSuffix = ".stage"
            versionNameSuffix = "-stage"
            signingConfig = signingConfigs["stage"]
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }
}

dependencies {

    implementation(fileTree(mapOf("include" to arrayOf("*.jar"), "dir" to "libs")))

    // cameraviewex
//    implementation(project(":cameraViewEx"))
    implementation("com.priyankvasa.android:cameraview-ex:3.2.0")

    // Kotlin
    implementation(Config.Libs.kotlinStdLibJdk8)

    // Coroutines
    implementation(Config.Libs.coroutinesCore)
    implementation(Config.Libs.coroutinesAndroid)

    // Android support
    implementation(Config.Libs.appcompatV7)
    implementation(Config.Libs.constraintLayout)

    // Firebase
    implementation(Config.Libs.firebaseCore)
    implementation(Config.Libs.firebaseMlVision)

    // Glide
    implementation(Config.Libs.glide) { exclude("com.android.support") }

    // Timber
    implementation(Config.Libs.timber)
}