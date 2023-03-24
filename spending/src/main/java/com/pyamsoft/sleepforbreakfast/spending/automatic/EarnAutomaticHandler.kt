package com.pyamsoft.sleepforbreakfast.spending.automatic

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction

internal abstract class EarnAutomaticHandler protected constructor() : BaseAutomaticHandler() {

  final override fun getType() = DbTransaction.Type.EARN
}
