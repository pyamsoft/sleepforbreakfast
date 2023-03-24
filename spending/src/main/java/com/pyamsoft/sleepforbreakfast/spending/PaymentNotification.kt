package com.pyamsoft.sleepforbreakfast.spending

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction

internal data class PaymentNotification(
    val title: String,
    val text: String,
    val amount: Long,
    val type: DbTransaction.Type,
)
