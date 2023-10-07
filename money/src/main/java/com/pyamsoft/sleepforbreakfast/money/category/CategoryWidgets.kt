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

package com.pyamsoft.sleepforbreakfast.money.category

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.sleepforbreakfast.db.category.CategoryChangeEvent
import com.pyamsoft.sleepforbreakfast.db.category.CategoryRealtime
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
interface CategoryIdMapper {

  fun bind(scope: CoroutineScope)

  @Composable @CheckResult fun collectAllCategories(): SnapshotStateList<DbCategory>

  @Composable @CheckResult fun map(id: DbCategory.Id): DbCategory

  @Composable
  @CheckResult
  fun map(ids: SnapshotStateList<DbCategory.Id>): SnapshotStateList<DbCategory>
}

@Stable
@Singleton
class DefaultCategoryIdMapper
@Inject
internal constructor(
    private val loader: CategoryLoader,
    private val categoryRealtime: CategoryRealtime,
) : CategoryIdMapper {

  private val cache = MutableStateFlow(setOf<DbCategory>())

  private suspend fun loadAllCategories() {
    loader.queryAllResult().onSuccess { list -> cache.value = list.toSet() }
  }

  @CheckResult
  private fun Set<DbCategory>.resolve(id: DbCategory.Id): DbCategory {
    return this.firstOrNull { it.id == id } ?: DbCategory.NONE
  }

  override fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Default) { loadAllCategories() }

    categoryRealtime.listenForChanges().also { f ->
      scope.launch(context = Dispatchers.Default) {
        f.collect { event ->
          when (event) {
            is CategoryChangeEvent.Delete -> {
              cache.update { c -> c.filterNot { it.id == event.category.id }.toSet() }
            }
            is CategoryChangeEvent.Insert -> {
              cache.update { it + event.category }
            }
            is CategoryChangeEvent.Update -> {
              cache.update { all ->
                all.map { c ->
                      if (c.id == event.category.id) {
                        event.category
                      } else {
                        c
                      }
                    }
                    .toSet()
              }
            }
          }
        }
      }
    }
  }

  @Composable
  override fun collectAllCategories(): SnapshotStateList<DbCategory> {
    return cache.collectAsStateListWithLifecycle()
  }

  @Composable
  override fun map(id: DbCategory.Id): DbCategory {
    val c by cache.collectAsStateWithLifecycle()
    return remember(id, c) { c.resolve(id) }
  }

  @Composable
  override fun map(ids: SnapshotStateList<DbCategory.Id>): SnapshotStateList<DbCategory> {
    val c by cache.collectAsStateWithLifecycle()
    return remember(ids, c) {
      val result = mutableSetOf<DbCategory>()
      for (id in ids) {
        val category = c.resolve(id)
        if (!category.id.isEmpty) {
          result.add(category)
        }
      }

      return@remember result.sortedBy { it.name.lowercase() }.toMutableStateList()
    }
  }
}
