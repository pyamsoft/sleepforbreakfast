package com.pyamsoft.sleepforbreakfast.spending.automatic

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction

internal abstract class SpendAutomaticHandler protected constructor() : BaseAutomaticHandler() {

  override fun getType() = DbTransaction.Type.SPEND
}
