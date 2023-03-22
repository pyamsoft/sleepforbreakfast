package com.pyamsoft.sleepforbreakfast.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.ui.app.installPYDroid
import com.pyamsoft.pydroid.ui.changelog.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.changelog.ChangeLogProvider
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.util.doOnCreate
import com.pyamsoft.pydroid.util.stableLayoutHideNavigation
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.R
import com.pyamsoft.sleepforbreakfast.SleepForBreakfastTheme
import com.pyamsoft.sleepforbreakfast.ui.InstallPYDroidExtras
import com.pyamsoft.sleepforbreakfast.work.enqueueActivityWork
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  @JvmField @Inject internal var themeViewModel: ThemeViewModeler? = null
  @JvmField @Inject internal var workerQueue: WorkerQueue? = null

  init {
    doOnCreate {
      installPYDroid(
          provider =
              object : ChangeLogProvider {

                override val applicationIcon = R.mipmap.ic_launcher

                override val changelog: ChangeLogBuilder = buildChangeLog {}
              },
      )
    }
  }

  private fun beginWork() {
    lifecycleScope.launch(context = Dispatchers.Default) {
      workerQueue.requireNotNull().enqueueActivityWork()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    stableLayoutHideNavigation()

    val component = ObjectGraph.ApplicationScope.retrieve(this).plusMainComponent().create(this)
    component.inject(this)
    ObjectGraph.ActivityScope.install(this, component)

    val tvm = themeViewModel.requireNotNull()
    val appName = getString(R.string.app_name)

    setContent {
      val themeState = tvm.state
      val theme by themeState.theme.collectAsState()

      SleepForBreakfastTheme(
          theme = theme,
      ) {
        SystemBars(
            theme = theme,
        )
        InstallPYDroidExtras()

        MainEntry(
            modifier = Modifier.fillMaxSize(),
            appName = appName,
        )
      }
    }
  }

  override fun onStart() {
    super.onStart()
    beginWork()
  }

  override fun onResume() {
    super.onResume()

    // Vitals
    themeViewModel.requireNotNull().handleSyncDarkTheme(this)
    reportFullyDrawn()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    themeViewModel?.handleSyncDarkTheme(this)
  }

  override fun onDestroy() {
    super.onDestroy()
    themeViewModel = null
  }
}
