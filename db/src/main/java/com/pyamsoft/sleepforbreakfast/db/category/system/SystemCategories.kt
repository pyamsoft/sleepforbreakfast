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

package com.pyamsoft.sleepforbreakfast.db.category.system

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface SystemCategories {

  @CheckResult suspend fun categoryByName(category: RequiredCategories): DbCategory?
}

@CheckResult
suspend fun SystemCategories.ensure() =
    withContext(context = Dispatchers.Default) {
      for (cat in RequiredCategories.values()) {
        categoryByName(cat).also { c ->
          if (c == null) {
            Timber.w("Failed to ensure creation of system category: $cat")
          }
        }
      }
    }
