package com.pyamsoft.sleepforbreakfast.worker.workmanager

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import timber.log.Timber

object WorkerObjectGraph {

  private val trackingMap = mutableMapOf<Application, WorkerComponent>()

  fun install(
      application: Application,
      component: WorkerComponent,
  ) {
    trackingMap[application] = component
    Timber.d("Track WorkerScoped install: $application $component")
  }

  @CheckResult
  fun retrieve(context: Context): WorkerComponent {
    return retrieve(context.applicationContext as Application)
  }

  @CheckResult
  fun retrieve(activity: Activity): WorkerComponent {
    return retrieve(activity.application)
  }

  @CheckResult
  fun retrieve(service: Service): WorkerComponent {
    return retrieve(service.application)
  }

  @CheckResult
  fun retrieve(application: Application): WorkerComponent {
    return trackingMap[application].requireNotNull {
      "Could not find WorkerScoped internals for Application: $application"
    }
  }
}
