package com.pyamsoft.sleepforbreakfast.db.room.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import java.time.DayOfWeek

internal object DayOfWeekConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toDate(date: String): DayOfWeek {
    return DayOfWeek.valueOf(date)
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromDate(date: DayOfWeek): String {
    return date.name
  }
}
