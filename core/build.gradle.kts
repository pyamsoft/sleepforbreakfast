/*
 * Copyright 2025 pyamsoft
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
  id("com.android.library")
  id("com.google.devtools.ksp")
  id("org.gradle.android.cache-fix")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.pyamsoft.sleepforbreakfast.core"

  compileSdk = rootProject.extra["compileSdk"] as Int

  defaultConfig { minSdk = rootProject.extra["minSdk"] as Int }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    // Flag to enable support for the new language APIs
    isCoreLibraryDesugaringEnabled = true
  }

  kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }

  buildFeatures { buildConfig = false }
}

dependencies {
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${rootProject.extra["desugar"]}")

  ksp("com.google.dagger:dagger-compiler:${rootProject.extra["dagger"]}")

  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.extra["coroutines"]}")

  api("com.jakewharton.timber:timber:${rootProject.extra["timber"]}")

  // PYDroid
  api("com.github.pyamsoft.pydroid:arch:${rootProject.extra["pydroid"]}")
  api("com.github.pyamsoft.pydroid:ui:${rootProject.extra["pydroid"]}")

  // Android support library.
  api("androidx.core:core-ktx:${rootProject.extra["core"]}")

  // Dagger
  api("com.google.dagger:dagger:${rootProject.extra["dagger"]}")
}
