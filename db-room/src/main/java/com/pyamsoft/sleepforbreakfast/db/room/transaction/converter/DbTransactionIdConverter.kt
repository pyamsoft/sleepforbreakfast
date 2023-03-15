package com.pyamsoft.sleepforbreakfast.db.room.transaction.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction

internal object DbTransactionIdConverter {

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun toId(id: String): DbTransaction.Id {
    return DbTransaction.Id(id)
  }

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun fromId(id: DbTransaction.Id): String {
    return id.raw
  }
}
