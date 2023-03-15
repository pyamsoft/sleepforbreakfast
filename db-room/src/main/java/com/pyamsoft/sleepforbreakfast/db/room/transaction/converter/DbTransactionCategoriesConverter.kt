package com.pyamsoft.sleepforbreakfast.db.room.transaction.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory

internal object DbTransactionCategoriesConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toCategories(categories: String): List<DbCategory.Id> {
    return categories.split("|").map { DbCategory.Id(it) }
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromCategories(categories: List<DbCategory.Id>): String {
    return categories.joinToString("|")
  }
}
