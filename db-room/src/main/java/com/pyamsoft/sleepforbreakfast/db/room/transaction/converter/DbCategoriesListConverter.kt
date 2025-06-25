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

package com.pyamsoft.sleepforbreakfast.db.room.transaction.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory

internal object DbCategoriesListConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toCategories(categories: String?): List<DbCategory.Id>? {
    if (categories == null) {
      return null
    }

    return categories.split("|").map { DbCategory.Id(it) }
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromCategories(categories: List<DbCategory.Id>?): String? {
    if (categories == null) {
      return null
    }

    return categories.joinToString("|") { it.raw }
  }
}
