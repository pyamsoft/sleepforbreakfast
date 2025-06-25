/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("com.android.application")
  id("com.google.devtools.ksp")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.gradle.android.cache-fix")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.pyamsoft.sleepforbreakfast"

  compileSdk = rootProject.extra["compileSdk"] as Int

  defaultConfig {
    applicationId = "com.pyamsoft.sleepforbreakfast"

    versionCode = 1
    versionName = "20250331-1"

    minSdk = rootProject.extra["minSdk"] as Int
    targetSdk = rootProject.extra["targetSdk"] as Int

    vectorDrawables.useSupportLibrary = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    // Flag to enable support for the new language APIs
    isCoreLibraryDesugaringEnabled = true
  }

  kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_17 } }

  signingConfigs {
    named("debug") {
      storeFile = file("debug.keystore")
      keyAlias = "androiddebugkey"
      keyPassword = "android"
      storePassword = "android"
    }
    create("release") {
      storeFile = file(project.findProperty("BUNDLE_STORE_FILE")?.toString() ?: "CANNOT BUILD")
      keyAlias = project.findProperty("BUNDLE_KEY_ALIAS")?.toString() ?: "CANNOT BUILD"
      keyPassword = project.findProperty("BUNDLE_KEY_PASSWD")?.toString() ?: "CANNOT BUILD"
      storePassword = project.findProperty("BUNDLE_STORE_PASSWD")?.toString() ?: "CANNOT BUILD"
    }
  }

  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("release")
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }

    debug {
      signingConfig = signingConfigs.getByName("debug")
      applicationIdSuffix = ".dev"
      versionNameSuffix = "-dev"
    }
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  // Fixes this error message
  // More than one file was found with OS independent path "META-INF/core_release.kotlin_module"
  packaging {
    resources.pickFirsts +=
        setOf(
            "META-INF/core_release.kotlin_module",
            "META-INF/ui_release.kotlin_module",
            "META-INF/INDEX.LIST",
            "META-INF/io.netty.versions.properties",
        )
  }
}

// Leave at bottom
// apply plugin: "com.google.gms.google-services"
dependencies {
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${rootProject.extra["desugar"]}")

  ksp("com.google.dagger:dagger-compiler:${rootProject.extra["dagger"]}")

  // Leak Canary
  debugImplementation(
      "com.squareup.leakcanary:leakcanary-android:${rootProject.extra["leakCanary"]}")
  implementation("com.squareup.leakcanary:plumber-android:${rootProject.extra["leakCanary"]}")

  // AndroidX
  implementation("androidx.appcompat:appcompat:${rootProject.extra["appCompat"]}")
  implementation("androidx.activity:activity-compose:${rootProject.extra["composeActivity"]}")

  // DataStore
  implementation("androidx.datastore:datastore-preferences:${rootProject.extra["dataStore"]}")

  // Needed just for androidx.preference.PreferenceManager
  // Eventually, big G may push for DataStore being a requirement, which will be pain
  // This pulls in all the UI bits too, which is a little lame.
  implementation("androidx.preference:preference:${rootProject.extra["preferences"]}")

  implementation(project(":automatic"))
  implementation(project(":category"))
  implementation(project(":core"))
  implementation(project(":db"))
  implementation(project(":db-room"))
  implementation(project(":home"))
  implementation(project(":main"))
  implementation(project(":money"))
  implementation(project(":spending"))
  implementation(project(":transactions"))
  implementation(project(":ui"))
  implementation(project(":worker"))
  implementation(project(":worker-workmanager"))
}
