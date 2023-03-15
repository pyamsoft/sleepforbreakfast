package com.pyamsoft.sleepforbreakfast.db.room.transaction.converter

import androidx.annotation.CheckResult
import androidx.room.TypeConverter
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction

internal object DbTransactionTypeConverter {

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun toType(type: String): DbTransaction.Type {
    return DbTransaction.Type.valueOf(type)
  }

  @JvmStatic
  @TypeConverter
  @CheckResult
  fun fromType(type: DbTransaction.Type): String {
    return type.name
  }
}
