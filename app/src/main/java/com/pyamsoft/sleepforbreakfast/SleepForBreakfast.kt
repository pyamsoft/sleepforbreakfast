/*
 * Copyright 2023 pyamsoft
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

package com.pyamsoft.sleepforbreakfast

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.libraries.OssLibraries
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.ui.installPYDroid
import com.pyamsoft.pydroid.util.isDebugMode
import com.pyamsoft.sleepforbreakfast.core.PRIVACY_POLICY_URL
import com.pyamsoft.sleepforbreakfast.core.TERMS_CONDITIONS_URL
import com.pyamsoft.sleepforbreakfast.work.enqueueAppWork
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerObjectGraph
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SleepForBreakfast : Application() {

  @Inject @JvmField internal var workerQueue: WorkerQueue? = null

  @CheckResult
  private fun installPYDroid(): ModuleProvider {
    val url = "https://github.com/pyamsoft/sleepforbreakfast"

    return installPYDroid(
        PYDroid.Parameters(
            viewSourceUrl = url,
            bugReportUrl = "$url/issues",
            privacyPolicyUrl = PRIVACY_POLICY_URL,
            termsConditionsUrl = TERMS_CONDITIONS_URL,
            version = BuildConfig.VERSION_CODE,
            logger = createLogger(),
            theme = SleepForBreakfastThemeProvider,
        ),
    )
  }

  private fun installWorkerComponent(component: BreakfastComponent) {
    val wc = component.plusWorkerComponent().create()
    WorkerObjectGraph.install(this, wc)
  }

  private fun installComponent(moduleProvider: ModuleProvider) {
    val mods = moduleProvider.get()
    val component =
        DaggerBreakfastComponent.factory()
            .create(
                debug = isDebugMode(),
                application = this,
                imageLoader = mods.imageLoader(),
                theming = mods.theming(),
                enforcer = mods.enforcer(),
            )
    component.inject(this)

    ObjectGraph.ApplicationScope.install(this, component)
    installWorkerComponent(component)
  }

  private fun beginWork() {
    // Coroutine start up is slow. What we can do instead is create a handler, which is cheap, and
    // post to the main thread to defer this work until after start up is done
    Handler(Looper.getMainLooper()).post {
      MainScope().launch(context = Dispatchers.Default) { workerQueue?.enqueueAppWork() }
    }
  }

  override fun onCreate() {
    super.onCreate()

    installLogger()
    val modules = installPYDroid()
    installComponent(modules)

    addLibraries()
    beginWork()
  }

  companion object {

    @JvmStatic
    private fun addLibraries() {
      // We are using pydroid-notify
      OssLibraries.usingNotify = true

      // We are using pydroid-autopsy
      OssLibraries.usingAutopsy = true

      OssLibraries.apply {
        add(
            "Room",
            "https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/room/",
            "The AndroidX Jetpack Room library. Fluent SQLite database access.",
        )
        add(
            "Cachify",
            "https://github.com/pyamsoft/cachify",
            "Simple in-memory caching of all the things",
        )
        add(
            "WorkManager",
            "https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/work/",
            "The AndroidX Jetpack WorkManager library. Schedule periodic work in a device friendly way.",
        )
        add(
            "Dagger",
            "https://github.com/google/dagger",
            "A fast dependency injector for Android and Java.",
        )
        add(
            "Accompanist System UI Controller",
            "https://google.github.io/accompanist/systemuicontroller/",
            "System UI Controller provides easy-to-use utilities for updating the System UI bar colors within Jetpack Compose.",
        )
      }
    }
  }
}
