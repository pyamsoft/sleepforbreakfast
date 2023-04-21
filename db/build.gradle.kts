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

plugins { id("com.android.library") }

android {
  namespace = "com.pyamsoft.sleepforbreakfast.db"

  compileSdk = rootProject.extra["compileSdk"] as Int

  defaultConfig {
    minSdk = rootProject.extra["minSdk"] as Int

    resourceConfigurations += setOf("en")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    // Flag to enable support for the new language APIs
    isCoreLibraryDesugaringEnabled = true
  }

  kotlinOptions { jvmTarget = "17" }

  buildFeatures { buildConfig = false }

  composeOptions {
    kotlinCompilerExtensionVersion = "${rootProject.extra["compose_compiler_version"]}"
  }
}

dependencies {
  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

  kapt("com.google.dagger:dagger-compiler:${rootProject.extra["dagger"]}")

  api("com.github.pyamsoft:cachify:0.3.0")

  api(project(":core"))
}
