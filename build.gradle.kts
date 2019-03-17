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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.3.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Config.Versions.kotlin}")
        classpath("com.google.gms:google-services:${Config.Versions.googleServices}") // google-services plugin
        classpath("com.github.dcendents:android-maven-gradle-plugin:${Config.Versions.mavenGradlePlugin}")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:${Config.Versions.bintrayPlugin}")
        classpath("org.jetbrains.dokka:dokka-android-gradle-plugin:${Config.Versions.dokka}")
    }
}

allprojects {

    repositories {
        google()
        mavenCentral()
        jcenter()
        maven {
            credentials(AwsCredentials::class) {
                setAccessKey("access-key")
                setSecretKey("secret-key")
            }
            url = uri("s3://pvasa-maven.s3.ca-central-1.amazonaws.com/")
        }
    }

    dependencies {
        configurations.all {
            resolutionStrategy.eachDependency {
                when (requested.group) {
                    "com.android.support" -> useVersion(Config.Versions.support)
                    "com.github.bumptech.glide" -> exclude("support-annotations")
                }
            }
        }
    }

    tasks.withType<KotlinCompile>().all {
        kotlinOptions {
            freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
            jvmTarget = Config.jvmTarget
        }
    }
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}