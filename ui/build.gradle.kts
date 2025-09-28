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
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.gradle.android.cache-fix")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.pyamsoft.sleepforbreakfast.ui"

  compileSdk = rootProject.extra["compileSdk"] as Int

  defaultConfig { minSdk = rootProject.extra["minSdk"] as Int }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    // Flag to enable support for the new language APIs
    isCoreLibraryDesugaringEnabled = true
  }

  kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }

  buildFeatures {
    buildConfig = false
    compose = true
  }
}

dependencies {
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${rootProject.extra["desugar"]}")

  ksp("com.google.dagger:dagger-compiler:${rootProject.extra["dagger"]}")
  ksp("com.squareup.moshi:moshi-kotlin-codegen:${rootProject.extra["moshi"]}")

  // Lifecycle extensions
  api("androidx.lifecycle:lifecycle-runtime-compose:${rootProject.extra["lifecycle"]}")

  // Compose
  api("androidx.compose.ui:ui:${rootProject.extra["compose"]}")
  api("androidx.compose.animation:animation:${rootProject.extra["compose"]}")
  api("androidx.compose.material3:material3:${rootProject.extra["composeMaterial3"]}")
  api("androidx.compose.material:material-icons-core:${rootProject.extra["composeMaterial"]}")

  // TODO(Peter): Remove after development is done
  api("androidx.compose.material:material-icons-extended:${rootProject.extra["composeMaterial"]}")

  // Compose Preview
  compileOnly("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["compose"]}")
  debugApi("androidx.compose.ui:ui-tooling:${rootProject.extra["compose"]}")

  api("com.squareup.moshi:moshi:${rootProject.extra["moshi"]}")

  implementation(project(":core"))
}
