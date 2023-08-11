package com.pyamsoft.sleepforbreakfast.db.transaction

import androidx.annotation.CheckResult

enum class SpendDirection {
  NONE,
  SPEND,
  EARN
}

@CheckResult
fun DbTransaction.Type.asDirection(): SpendDirection =
    when (this) {
      DbTransaction.Type.SPEND -> SpendDirection.SPEND
      DbTransaction.Type.EARN -> SpendDirection.EARN
    }
