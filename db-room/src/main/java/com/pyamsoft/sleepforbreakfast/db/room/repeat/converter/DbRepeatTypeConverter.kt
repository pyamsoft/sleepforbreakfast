package com.pyamsoft.sleepforbreakfast.db.room.repeat.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat

internal object DbRepeatTypeConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toType(type: String): DbRepeat.Type {
    return type.let { DbRepeat.Type.valueOf(it) }
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromType(type: DbRepeat.Type): String {
    return type.name
  }
}
