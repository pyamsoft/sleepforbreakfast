package com.pyamsoft.sleepforbreakfast.home.notification

import android.content.Intent
import android.provider.Settings
import androidx.annotation.CheckResult
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.core.Enforcer
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class NotificationListenerStatusImpl
@Inject
internal constructor(
    private val activity: FragmentActivity,
) : NotificationListenerStatus {

  private val status = MutableStateFlow(isNotificationListenerEnabled())

  init {
    activity.lifecycle.addObserver(
        object : DefaultLifecycleObserver {
          override fun onResume(owner: LifecycleOwner) {
            status.value = isNotificationListenerEnabled()
          }

          override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
          }
        },
    )
  }

  @CheckResult
  private fun isNotificationListenerEnabled(): Boolean {
    val context = activity.applicationContext
    val listeners = NotificationManagerCompat.getEnabledListenerPackages(context)
    return listeners.contains(context.packageName)
  }

  @CheckResult
  private fun tryOpenIntent(intent: Intent): Boolean {
    return try {
      activity.startActivity(intent)
      true
    } catch (e: android.content.ActivityNotFoundException) {
      Timber.e(e, "Could not open intent: ${intent.action}")
      false
    }
  }

  @CheckResult
  private fun openSettingsPageIntent(action: String): Boolean {
    // Try specific first, may fail on some devices
    var intent = Intent(action, "package:${activity.packageName}".toUri())
    if (!tryOpenIntent(intent)) {
      Timber.w("Failed specific intent for $action")
      intent = Intent(action)
      if (!tryOpenIntent(intent)) {
        Timber.w("Failed generic intent for $action")
        return false
      }
    }

    return true
  }

  override fun isNotificationListenerActive(): Flow<Boolean> {
    return status
  }

  override suspend fun activateNotificationListener() =
      withContext(context = Dispatchers.Main) {
        Enforcer.assertOnMainThread()

        val action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        if (openSettingsPageIntent(action)) {
          Timber.w("Failed to open settings page: $action")
        }
      }
}
