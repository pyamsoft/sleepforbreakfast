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

package com.pyamsoft.sleepforbreakfast.db.room.category.system

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.category.system.RequiredCategories
import com.pyamsoft.sleepforbreakfast.db.category.system.SystemCategories
import com.pyamsoft.sleepforbreakfast.db.room.category.entity.RoomDbCategory
import java.time.Clock
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class SystemCategoriesImpl
internal constructor(
    // Provided by Dagger in RoomModule
    private val systemCategories: RoomSystemCategories,
    // Provided by Dagger from BreakfastComponent
    private val clock: Clock,
) : SystemCategories {

  @CheckResult
  private fun noteForName(category: RequiredCategories): String {
    val what =
        when (category) {
          RequiredCategories.VENMO -> "Venmo related transactions"
          RequiredCategories.VENMO_PAY -> "Venmo payments"
          RequiredCategories.VENMO_REQUESTS -> "Venmo requests"
          RequiredCategories.GOOGLE_WALLET -> "Google Wallet spending notifications"
          RequiredCategories.REPEATING -> "Repeating Transactions"
        }

    return "System Category for $what"
  }

  @CheckResult
  private fun createSystemCategory(category: RequiredCategories): RoomDbCategory {
    val raw =
        DbCategory.create(
                clock,
                id = DbCategory.Id(IdGenerator.generate()),
            )
            .systemLevel()
            .name(category.displayName)
            .note(noteForName(category))
    return RoomDbCategory.create(raw)
  }

  override suspend fun categoryByName(category: RequiredCategories): DbCategory? =
      withContext(context = Dispatchers.IO) {
        systemCategories.categoryByName(category) { createSystemCategory(it) }
      }

  override suspend fun ensure() =
      withContext(context = Dispatchers.IO) {
        for (cat in RequiredCategories.values()) {
          categoryByName(cat).also { c ->
            if (c == null) {
              Timber.w("Failed to ensure creation of system category: $cat")
            }
          }
        }
      }
}
