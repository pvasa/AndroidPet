// Copyright 2019 Priyank Vasa
//
// Copyright (C) 2016 The Android Open Source Project
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

import groovy.util.Node
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaAndroidTask
import org.jetbrains.dokka.gradle.LinkMapping
import org.jetbrains.kotlin.gradle.internal.AndroidExtensionsExtension
import org.jetbrains.kotlin.gradle.internal.CacheImplementation
import java.net.URL

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("org.jetbrains.dokka-android")
    id("maven-publish")
}

val srcDirs: Array<out String> = arrayOf(
    "src/main/java",
    "src/main/base",
    "src/main/api14",
    "src/main/api21",
    "src/main/api23",
    "src/main/api24"
)

group = "com.priyankvasa.android"
version = Config.versionName
description = "CameraViewEx highly simplifies integration of camera implementation and various camera features into any Android project. It uses new camera2 api with advanced features on API level 21 and higher and smartly switches to camera1 on older devices (API < 21)."

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
        minSdkVersion(Config.Android.minSdkLib)
        targetSdkVersion(Config.Android.sdk)
        versionCode = Config.versionCode
        versionName = Config.versionName
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("proguard-rules.pro")
        renderscriptTargetApi = 21
    }

    testBuildType = "stage"

    buildTypes {

        getByName("debug") {
            isDebuggable = true
        }

        getByName("release") {
            initWith(buildTypes["debug"])
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["release"]
        }

        create("stage").apply {
            initWith(buildTypes["release"])
            signingConfig = signingConfigs["stage"]
        }
    }

    sourceSets["main"].java.srcDirs(*srcDirs)

    sourceSets["main"].renderscript.srcDirs(
        "src/main/rs"
    )

    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }
}

androidExtensions {
    configure(delegateClosureOf<AndroidExtensionsExtension> {
        isExperimental = true
        defaultCacheImplementation = CacheImplementation.SPARSE_ARRAY
    })
}

tasks.withType(Test::class.java) {
    useJUnitPlatform {
        //includeTags "fast", "smoke & feature-a"
        //excludeTags "slow", "ci"
        includeEngines = setOf("junit-jupiter")
        excludeEngines = setOf("junit-vintage")
    }
    systemProperty("java.util.logging.manager", "java.util.logging.LogManager")
    systemProperty("junit.jupiter.conditions.deactivate", "*")
    systemProperties = mutableMapOf<String, Any>(
        "junit.jupiter.extensions.autodetection.enabled" to "true",
        "junit.jupiter.testinstance.lifecycle.default" to "per_class"
    )
}

dependencies {

    // Kotlin
    implementation(Config.Libs.kotlinStdLibJdk8)

    // Android support
    implementation(Config.Libs.supportAnnotations)
    implementation(Config.Libs.supportTransition)
    implementation(Config.Libs.supportExifInterface)

    // Dependency injection
    implementation(Config.Libs.koin)

    // KotlinX
    implementation(Config.Libs.coroutinesCore)
    implementation(Config.Libs.coroutinesAndroid)

    // Lifecycle
    implementation(Config.Libs.lifecycleLivedata)

    // Timber
    implementation(Config.Libs.timber)

    // Unit tests
    // (Required) Writing and executing Unit Tests on the JUnit Platform
    testImplementation(Config.TestLibs.junitJupiterApi)
    testRuntimeOnly(Config.TestLibs.junitJupiterEngine)

    // (Optional) If you need "Parameterized Tests"
    testImplementation(Config.TestLibs.junitJupiterParams)

    // Android tests
    androidTestImplementation(Config.AndroidTestLibs.junit) { exclude("support-annotations") }
    androidTestImplementation(Config.AndroidTestLibs.testRules) { exclude("support-annotations") }
    androidTestImplementation(Config.AndroidTestLibs.testEspressoCore) { exclude("support-annotations") }
}

tasks.getByName<DokkaAndroidTask>("dokka") {

    moduleName = "cameraViewEx"
    outputFormat = "html"
    outputDirectory = "docs"

    // These tasks will be used to determine source directories and classpath
    kotlinTasks(closureOf<Any?> { defaultKotlinTasks() + tasks["compileDebugKotlin"] })

    // List of files with module and package documentation
    // http://kotlinlang.org/docs/reference/kotlin-doc.html#module-and-package-documentation
    includes = listOf("../README.md")

    // The list of files or directories containing sample code (referenced with @sample tags)
    // samples = listOf("src/main/sample")

    jdkVersion = 8 // Used for linking to JDK

    // Use default or set to custom path to cache directory
    // to enable package-list caching
    // When set to default, caches stored in $USER_HOME/.cache/dokka
    cacheRoot = "default"

    // Use to include or exclude non public members.
    // TODO: Set to true after adding more detailed documentation to non public code
    includeNonPublic = false

    // Do not output deprecated members. Applies globally, can be overridden by packageOptions
    skipDeprecated = false

    // Emit warnings about not documented members. Applies globally, also can be overridden by packageOptions
    reportUndocumented = false

    skipEmptyPackages = true // Do not create index pages for empty packages

    impliedPlatforms = mutableListOf("JVM") // See platforms section of documentation

    // By default, sourceRoots is taken from kotlinTasks, following roots will be appended to it
    // Short form sourceRoots
    sourceDirs = files(*srcDirs)

    srcDirs.forEach {
        linkMapping(delegateClosureOf<LinkMapping> {
            dir = it
            url = "https://github.com/pvasa/cameraview-ex/tree/master/cameraViewEx/$it"
            suffix = "#L"
        })
    }

    // No default documentation link to kotlin-stdlib
    noStdlibLink = false

    // Allows linking to documentation of the project's dependencies (generated with Javadoc or Dokka)
    // Repeat for multiple links
    externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
        // Root URL of the generated documentation to link with. The trailing slash is required!
        url = URL("https://developer.android.com/reference/packages/")

        // If package-list file located in non-standard location
        packageListUrl = URL("https://developer.android.com/reference/package-list")
    })

    externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
        url = URL("https://docs.oracle.com/javase/8/docs/api/")
        packageListUrl = URL("https://docs.oracle.com/javase/8/docs/api/package-list")
    })

    externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
        url = URL("https://developer.android.com/reference/kotlin/packages/")
        // TODO: Uncomment once this is supported. Currently it throws RuntimeException: Failed to parse package list from https://developer.android.com/reference/kotlin/package-list
        // packageListUrl = URL("https://developer.android.com/reference/kotlin/package-list")
    })

    externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
        url = URL("https://developer.android.com/reference/kotlin/androidx/packages/")
        // TODO: Not available yet. Uncomment when available.
        // packageListUrl = URL("https://developer.android.com/reference/kotlin/androidx/package-list")
    })

    externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
        url = URL("https://developer.android.com/reference/android/support/packages/")
        packageListUrl = URL("https://developer.android.com/reference/android/support/package-list")
    })

    externalDocumentationLink(delegateClosureOf<DokkaConfiguration.ExternalDocumentationLink.Builder> {
        url = URL("https://developer.android.com/reference/android/arch/packages/")
        packageListUrl = URL("https://developer.android.com/reference/android/arch/package-list")
    })
}

tasks.register<Jar>("sourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
    classifier = "sources"
}

publishing {

    publications {

        create<MavenPublication>("cameraViewEx") {

            groupId = project.group.toString()
            artifactId = "cameraview-ex"
            version = project.version.toString()

            artifact("$buildDir/outputs/aar/cameraViewEx-release.aar")
            artifact(tasks["sourcesJar"])

            pom {
                packaging = "aar"
                name.set("CameraViewEx")
                description.set(project.description)
                url.set("https://github.com/pvasa/cameraview-ex")
                inceptionYear.set("2018")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("pvasa")
                        name.set("Priyank Vasa")
                        email.set("pv.ryan14@gmail.com")
                        organization.set("TradeRev")
                        organizationUrl.set("https://www.traderev.com/en-ca/")
                        url.set("https://priyankvasa.dev")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/cameraview-ex.git")
                    developerConnection.set("scm:git:ssh://github.com/cameraview-ex.git")
                    url.set("https://github.com/cameraview-ex/")
                }
                withXml {
                    val dependencies: Node = asNode().appendNode("dependencies")
                    fun appendDependency(dependency: Dependency, scope: String) {
                        dependencies.appendNode("dependency").apply {
                            appendNode("groupId", dependency.group)
                            appendNode("artifactId", dependency.name)
                            appendNode("version", dependency.version)
                            appendNode("scope", scope)
                        }
                    }
                    configurations.implementation.dependencies.forEach { appendDependency(it, "runtime") }
                }
            }
        }
    }

    repositories {

        maven {
            credentials(AwsCredentials::class) {
                setAccessKey("access-key")
                setSecretKey("secret-key")
            }
            url = uri("s3://pvasa-maven.s3.ca-central-1.amazonaws.com/")
        }
    }
}

/*
apply {
    from("publish.gradle")
}*/