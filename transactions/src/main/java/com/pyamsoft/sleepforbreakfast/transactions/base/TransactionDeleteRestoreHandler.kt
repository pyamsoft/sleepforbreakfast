package com.pyamsoft.sleepforbreakfast.transactions.base

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.helper.DeleteRestoreHandlerImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransactionDeleteRestoreHandler @Inject internal constructor() :
    DeleteRestoreHandlerImpl<DbTransaction>()
