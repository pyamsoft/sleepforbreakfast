package com.pyamsoft.sleepforbreakfast.db.transaction

import androidx.annotation.CheckResult

enum class SpendDirection {
  NONE,
  LOSS,
  GAIN
}

@CheckResult
fun DbTransaction.Type.asDirection(): SpendDirection =
    when (this) {
      DbTransaction.Type.SPEND -> SpendDirection.LOSS
      DbTransaction.Type.EARN -> SpendDirection.GAIN
    }
