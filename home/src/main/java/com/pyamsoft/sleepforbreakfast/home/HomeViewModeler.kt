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

package com.pyamsoft.sleepforbreakfast.home

import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.sleepforbreakfast.home.notification.NotificationListenerStatus
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModeler
@Inject
internal constructor(
    state: MutableHomeViewState,
    private val listenerStatus: NotificationListenerStatus,
) : AbstractViewModeler<HomeViewState>(state) {

  private val vmState = state

  fun bind(scope: CoroutineScope) {
    listenerStatus.isNotificationListenerActive().also { f ->
      scope.launch(context = Dispatchers.Default) {
        f.collect { vmState.isNotificationListenerEnabled.value = it }
      }
    }
  }

  fun handleOpenNotificationSettings(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Default) { listenerStatus.activateNotificationListener() }
  }
}
