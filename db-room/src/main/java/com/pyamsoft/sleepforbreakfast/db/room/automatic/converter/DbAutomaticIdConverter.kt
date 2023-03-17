package com.pyamsoft.sleepforbreakfast.db.room.automatic.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic

internal object DbAutomaticIdConverter {

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun toId(id: String?): DbAutomatic.Id? {
    return id?.let { DbAutomatic.Id(it) }
  }

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun fromId(id: DbAutomatic.Id?): String? {
    return id?.raw
  }
}
