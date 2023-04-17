/*
 * Copyright 2021 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins { id("com.android.application") }

android {
  namespace = "com.pyamsoft.sleepforbreakfast"

  compileSdk = rootProject.extra["compileSdk"] as Int

  defaultConfig {
    applicationId = "com.pyamsoft.sleepforbreakfast"

    versionCode = 1
    versionName = "20230313-1"

    minSdk = rootProject.extra["minSdk"] as Int
    targetSdk = rootProject.extra["targetSdk"] as Int

    resourceConfigurations += setOf("en")

    vectorDrawables.useSupportLibrary = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    // Flag to enable support for the new language APIs
    isCoreLibraryDesugaringEnabled = true
  }

  kotlinOptions { jvmTarget = "17" }

  signingConfigs {
    getByName("debug") {
      storeFile = file("debug.keystore")
      keyAlias = "androiddebugkey"
      keyPassword = "android"
      storePassword = "android"
    }
    create("release") {
      storeFile = File(System.getenv("BUNDLE_STORE_FILE").orEmpty())
      keyAlias = System.getenv("BUNDLE_KEY_ALIAS").orEmpty()
      keyPassword = System.getenv("BUNDLE_KEY_PASSWD").orEmpty()
      storePassword = System.getenv("BUNDLE_STORE_PASSWD").orEmpty()
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

  composeOptions {
    kotlinCompilerExtensionVersion = "${rootProject.extra["compose_compiler_version"]}"
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
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

  kapt("com.google.dagger:dagger-compiler:${rootProject.extra["dagger"]}")

  // Leak Canary
  debugImplementation(
      "com.squareup.leakcanary:leakcanary-android:${rootProject.extra["leakCanary"]}")
  implementation("com.squareup.leakcanary:plumber-android:${rootProject.extra["leakCanary"]}")

  // Autopsy
  debugImplementation("com.github.pyamsoft.pydroid:autopsy:${rootProject.extra["pydroid"]}")

  // AndroidX
  api("androidx.fragment:fragment-ktx:${rootProject.extra["fragment"]}")
  api("androidx.appcompat:appcompat:${rootProject.extra["appCompat"]}")

  api("com.google.accompanist:accompanist-systemuicontroller:${rootProject.extra["accompanist"]}")

  implementation(project(":category"))
  implementation(project(":core"))
  implementation(project(":db"))
  implementation(project(":db-room"))
  implementation(project(":home"))
  implementation(project(":main"))
  implementation(project(":money"))
  implementation(project(":repeat"))
  implementation(project(":spending"))
  implementation(project(":transactions"))
  implementation(project(":ui"))
  implementation(project(":worker"))
  implementation(project(":worker-workmanager"))
}
