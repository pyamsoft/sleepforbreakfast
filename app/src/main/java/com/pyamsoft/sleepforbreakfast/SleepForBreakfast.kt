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

class SleepForBreakfast : Application() {

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

  private fun installComponent(moduleProvider: ModuleProvider) {
    val mods = moduleProvider.get()
    val component =
        DaggerBreakfastComponent.factory()
            .create(
                application = this,
                debug = isDebugMode(),
                imageLoader = mods.imageLoader(),
                theming = mods.theming(),
            )
    component.inject(this)

    ObjectGraph.ApplicationScope.install(this, component)
  }

  override fun onCreate() {
    super.onCreate()

    installLogger()
    val modules = installPYDroid()
    installComponent(modules)

    addLibraries()
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

        add(
            "Accompanist Pager",
            "https://google.github.io/accompanist/pager/",
            "A library which provides paging layouts for Jetpack Compose.",
        )
      }
    }
  }
}
