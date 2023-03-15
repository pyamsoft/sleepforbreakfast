package com.pyamsoft.sleepforbreakfast.preference

import android.content.Context
import androidx.preference.PreferenceManager
import com.pyamsoft.pydroid.core.Enforcer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PreferencesImpl
@Inject
internal constructor(
    private val context: Context,
) {

  private val preferences by lazy {
    Enforcer.assertOffMainThread()
    PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
  }
}
