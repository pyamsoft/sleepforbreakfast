package com.pyamsoft.sleepforbreakfast.db.room.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal object LocalTimeConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toTime(time: String): LocalTime {
    return LocalTime.parse(time, DateTimeFormatter.ISO_LOCAL_TIME)
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromTime(time: LocalTime): String {
    return DateTimeFormatter.ISO_LOCAL_TIME.format(time)
  }
}
