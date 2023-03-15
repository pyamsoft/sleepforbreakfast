package com.pyamsoft.sleepforbreakfast.transactions.delete

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.squareup.moshi.JsonClass

@Stable
data class TransactionDeleteParams(
    val transactionId: DbTransaction.Id,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        transactionId = transactionId.raw,
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json(
      val transactionId: String,
  ) {

    @CheckResult
    fun fromJson(): TransactionDeleteParams {
      return TransactionDeleteParams(
          transactionId = DbTransaction.Id(transactionId),
      )
    }
  }
}
