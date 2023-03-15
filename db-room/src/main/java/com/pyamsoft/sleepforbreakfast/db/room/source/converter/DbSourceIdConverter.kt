package com.pyamsoft.sleepforbreakfast.db.room.source.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.sleepforbreakfast.db.source.DbSource

internal object DbSourceIdConverter {

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun toId(id: String?): DbSource.Id? {
    return id?.let { DbSource.Id(it) }
  }

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun fromId(id: DbSource.Id?): String? {
    return id?.raw
  }
}
