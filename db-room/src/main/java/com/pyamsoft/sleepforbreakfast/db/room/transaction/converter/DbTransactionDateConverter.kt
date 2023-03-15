package com.pyamsoft.sleepforbreakfast.db.room.transaction.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal object DbTransactionDateConverter {

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun toDate(date: String): LocalDateTime {
    return LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  }

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun fromDate(date: LocalDateTime): String {
    return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date)
  }
}
