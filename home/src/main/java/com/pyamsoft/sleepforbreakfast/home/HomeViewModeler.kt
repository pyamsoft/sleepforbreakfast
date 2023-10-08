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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.home.notification.NotificationListenerStatus
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class HomeViewModeler
@Inject
internal constructor(
    override val state: MutableHomeViewState,
    private val listenerStatus: NotificationListenerStatus,
    private val categoryLoader: CategoryLoader,
) : HomeViewState by state, AbstractViewModeler<HomeViewState>(state) {

  @CheckResult
  private suspend fun fetchCategories(): List<DbCategory> {
    try {
      return categoryLoader.query()
    } catch (e: Throwable) {
      e.ifNotCancellation {
        Timber.e(e) { "Error fetching categories" }
        return emptyList()
      }
    }
  }

  private suspend fun CoroutineScope.load() {
    val scope = this

    if (state.loading.value == LoadingState.LOADING) {
      Timber.d { "Already loading" }
      return
    }
    state.loading.value = LoadingState.LOADING

    val jobs = mutableListOf<Deferred<*>>()
    jobs.add(
        scope.async(context = Dispatchers.Default) {
          val categories = fetchCategories()
          state.categories.value = categories.sortedBy { it.name.lowercase() }
        },
    )

    try {
      jobs.awaitAll()
    } finally {
      state.loading.value = LoadingState.DONE
    }
  }

  fun bind(scope: CoroutineScope) {
    listenerStatus.isNotificationListenerActive().also { f ->
      scope.launch(context = Dispatchers.Default) {
        f.collect { state.isNotificationListenerEnabled.value = it }
      }
    }

    scope.launch(context = Dispatchers.Default) { load() }
  }

  fun handleOpenNotificationSettings(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Default) { listenerStatus.activateNotificationListener() }
  }
}
