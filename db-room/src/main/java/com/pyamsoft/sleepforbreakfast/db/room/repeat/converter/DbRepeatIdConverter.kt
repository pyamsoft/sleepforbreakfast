package com.pyamsoft.sleepforbreakfast.db.room.repeat.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat

internal object DbRepeatIdConverter {

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun toId(id: String?): DbRepeat.Id? {
    return id?.let { DbRepeat.Id(it) }
  }

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun fromId(id: DbRepeat.Id?): String? {
    return id?.raw
  }
}
