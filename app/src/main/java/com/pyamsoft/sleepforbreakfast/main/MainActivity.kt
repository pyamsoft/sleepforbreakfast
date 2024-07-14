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

package com.pyamsoft.sleepforbreakfast.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.app.PYDroidActivityDelegate
import com.pyamsoft.pydroid.ui.app.installPYDroid
import com.pyamsoft.pydroid.ui.changelog.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.changelog.ChangeLogProvider
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.ui.util.fillUpToPortraitSize
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.R
import com.pyamsoft.sleepforbreakfast.SleepForBreakfastTheme
import com.pyamsoft.sleepforbreakfast.ui.InstallPYDroidExtras
import com.pyamsoft.sleepforbreakfast.work.enqueueActivityWork
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

  @JvmField @Inject internal var themeViewModel: ThemeViewModeler? = null
  @JvmField @Inject internal var workerQueue: WorkerQueue? = null

  private var pydroid: PYDroidActivityDelegate? = null

  private fun beginWork() {
    lifecycleScope.launch(context = Dispatchers.Default) {
      workerQueue.requireNotNull().enqueueActivityWork()
    }
  }

  private fun initializePYDroid() {
    pydroid = installPYDroid(
        provider =
            object : ChangeLogProvider {

              override val applicationIcon = R.mipmap.ic_launcher_round

              override val changelog: ChangeLogBuilder = buildChangeLog {}
            },
    )
  }

  private fun setupActivity() {
    // Setup PYDroid first
    initializePYDroid()

    // Create and initialize the ObjectGraph
    val component = ObjectGraph.ApplicationScope.retrieve(this).plusMainComponent().create(this)
    component.inject(this)
    ObjectGraph.ActivityScope.install(this, component)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupActivity()

    val vm = themeViewModel.requireNotNull()
    val appName = getString(R.string.app_name)

    setContent {
      val theme by vm.theme.collectAsStateWithLifecycle()
      val isMaterialYou by vm.isMaterialYou.collectAsStateWithLifecycle()

      SaveStateDisposableEffect(vm)

      SleepForBreakfastTheme(
          theme = theme,
          isMaterialYou = isMaterialYou,
      ) {
        InstallPYDroidExtras(
            modifier = Modifier.fillUpToPortraitSize(),
            appName = appName,
        )

        MainEntry(
            modifier = Modifier.fillMaxSize(),
            appName = appName,
            theme = theme,
        )
      }
    }

    vm.init(this)
  }

  override fun onStart() {
    super.onStart()
    beginWork()
  }

  override fun onResume() {
    super.onResume()
    reportFullyDrawn()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    themeViewModel = null
    pydroid = null
  }
}
