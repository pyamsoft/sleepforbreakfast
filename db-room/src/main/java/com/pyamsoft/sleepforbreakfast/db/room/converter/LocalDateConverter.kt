package com.pyamsoft.sleepforbreakfast.db.room.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object LocalDateConverter {

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun toDate(date: String): LocalDate {
    return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
  }

  @JvmStatic
  @CheckResult
  @TypeConverter
  fun fromDate(date: LocalDate): String {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date)
  }
}
