package com.pyamsoft.sleepforbreakfast.db.room.category.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory

internal object DbCategoryIdConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toId(id: String?): DbCategory.Id? {
    return id?.let { DbCategory.Id(it) }
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromId(id: DbCategory.Id?): String? {
    return id?.raw
  }
}
