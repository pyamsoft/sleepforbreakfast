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

package com.pyamsoft.sleepforbreakfast

import android.app.Application
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.libraries.OssLibraries
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.ui.installPYDroid
import com.pyamsoft.pydroid.util.isDebugMode
import com.pyamsoft.sleepforbreakfast.core.PRIVACY_POLICY_URL
import com.pyamsoft.sleepforbreakfast.core.TERMS_CONDITIONS_URL
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.GuaranteedSpending
import com.pyamsoft.sleepforbreakfast.work.enqueueAppWork
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerObjectGraph
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class SleepForBreakfast : Application() {

    @Inject
    @JvmField
    internal var workerQueue: WorkerQueue? = null

    @Inject
    @JvmField
    internal var guaranteedSpending: GuaranteedSpending? = null

    @CheckResult
    private fun initPYDroid(): ModuleProvider {
        val url = "https://github.com/pyamsoft/sleepforbreakfast"

        return installPYDroid(
            PYDroid.Parameters(
                viewSourceUrl = url,
                bugReportUrl = "$url/issues",
                privacyPolicyUrl = PRIVACY_POLICY_URL,
                termsConditionsUrl = TERMS_CONDITIONS_URL,
                version = BuildConfig.VERSION_CODE,
                logger = createLogger(),
            ),
        )
    }

    private fun installWorkerComponent(component: BreakfastComponent) {
        val wc = component.plusWorkerComponent().create()
        WorkerObjectGraph.install(this, wc)
    }

    private fun installObjectGraph(component: BreakfastComponent) {
        ObjectGraph.ApplicationScope.install(this, component)
        installWorkerComponent(component)
    }

    private fun installComponent(
        scope: CoroutineScope,
        moduleProvider: ModuleProvider,
    ) {
        val mods = moduleProvider.get()
        val component =
            DaggerBreakfastComponent.factory()
                .create(
                    debug = isDebugMode(),
                    scope = scope,
                    application = this,
                    theming = mods.theming(),
                    enforcer = mods.enforcer(),
                )

        installObjectGraph(component)
        component.inject(this)
    }

    private fun beginWork(scope: CoroutineScope) {
        scope.launch(context = Dispatchers.Default) {
            guaranteedSpending?.ensureExistsInDatabase()
            workerQueue?.enqueueAppWork()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val modules = initPYDroid()

        val scope =
            CoroutineScope(
                context = SupervisorJob() + Dispatchers.Default + CoroutineName(this::class.java.name),
            )
        installLogger(
            scope = scope,
            inAppDebugStatus = modules.get().inAppDebugStatus(),
        )

        installComponent(scope, modules)
        addLibraries()
        beginWork(scope)
    }

    companion object {

        @JvmStatic
        private fun addLibraries() {
            OssLibraries.apply {
                usingAutopsy = true
                usingArch = true
                usingUi = true
            }

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
                    "Vico",
                    "https://github.com/patrykandpatrick/vico",
                    "A light and extensible chart library for Android.",
                )

                add(
                    "Jetpack Compose Color Picker",
                    "https://github.com/godaddy/compose-color-picker",
                    "A component that provides two different HSV color pickers, written in Jetpack Compose.",
                )
            }
        }
    }
}
