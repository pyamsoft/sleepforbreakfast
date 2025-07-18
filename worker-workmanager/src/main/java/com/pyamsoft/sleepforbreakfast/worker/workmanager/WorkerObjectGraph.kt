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

package com.pyamsoft.sleepforbreakfast.worker.workmanager

import android.app.Activity
import android.app.Application
import android.app.Service
import androidx.annotation.CheckResult
import androidx.work.ListenableWorker
import com.pyamsoft.pydroid.core.cast
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.sleepforbreakfast.core.Timber

object WorkerObjectGraph {

  private val trackingMap = mutableMapOf<Application, WorkerComponent>()

  fun install(
      application: Application,
      component: WorkerComponent,
  ) {
    trackingMap[application] = component
    Timber.d { "Track WorkerScoped install: $application $component" }
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
  fun retrieve(worker: ListenableWorker): WorkerComponent {
    return retrieve(worker.applicationContext.cast<Application>().requireNotNull())
  }

  @CheckResult
  fun retrieve(application: Application): WorkerComponent {
    return trackingMap[application].requireNotNull {
      "Could not find WorkerScoped internals for Application: $application"
    }
  }
}
