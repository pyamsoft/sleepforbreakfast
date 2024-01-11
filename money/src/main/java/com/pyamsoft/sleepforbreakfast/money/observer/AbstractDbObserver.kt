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

package com.pyamsoft.sleepforbreakfast.money.observer

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbQuery
import com.pyamsoft.sleepforbreakfast.db.DbRealtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal abstract class AbstractDbObserver<T : Any, R : Any, I : Any>
protected constructor(
    private val query: DbQuery<T>,
    private val realtime: DbRealtime<R>,
) : BaseDbObserver<T, I> {

  private val cache = MutableStateFlow(setOf<T>())

  private suspend fun loadAllCategories() {
    try {
      val list = query.query()
      cache.value = list.toSet()
    } catch (e: Throwable) {
      Timber.e(e) { "Error during observer initial query" }
      cache.value = emptySet()
    }
  }

  @CheckResult
  private fun Set<T>.resolve(id: I): T {
    return this.firstOrNull { dataToId(it) == id } ?: emptyInstance
  }

  final override fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Default) { loadAllCategories() }

    realtime.listenForChanges().also { f ->
      scope.launch(context = Dispatchers.Default) { f.collect { onRealtimeEvent(it) } }
    }
  }

  @Composable
  final override fun collect(): SnapshotStateList<T> {
    return cache.collectAsStateListWithLifecycle()
  }

  @Composable
  final override fun map(id: I): T {
    val c by cache.collectAsStateWithLifecycle()
    return remember(id, c) { c.resolve(id) }
  }

  @Composable
  final override fun map(ids: SnapshotStateList<I>): SnapshotStateList<T> {
    val c by cache.collectAsStateWithLifecycle()
    return remember(ids, c) {
      val result = mutableSetOf<T>()
      for (id in ids) {
        val category = c.resolve(id)
        if (!isEmpty(category)) {
          result.add(category)
        }
      }

      return@remember result.sortedBy { sortData(it) }.toMutableStateList()
    }
  }

  protected fun handleRealtimeDelete(data: T) {
    val d = dataToId(data)
    cache.update { c -> c.filterNot { dataToId(it) == d }.toSet() }
  }

  protected fun handleRealtimeInsert(data: T) {
    cache.update { it + data }
  }

  protected fun handleRealtimeUpdate(data: T) {
    val d = dataToId(data)
    cache.update { all ->
      all.map { c ->
            if (dataToId(c) == d) {
              data
            } else {
              c
            }
          }
          .toSet()
    }
  }

  protected abstract val emptyInstance: T

  protected abstract suspend fun onRealtimeEvent(event: R)

  @CheckResult protected abstract fun sortData(data: T): String

  @CheckResult protected abstract fun isEmpty(data: T): Boolean

  @CheckResult protected abstract fun dataToId(data: T): I
}
