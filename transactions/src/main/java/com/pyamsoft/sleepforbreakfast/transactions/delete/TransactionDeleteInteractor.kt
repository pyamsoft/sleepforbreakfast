package com.pyamsoft.sleepforbreakfast.transactions.delete

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.list.ListDeleteInteractor

internal interface TransactionDeleteInteractor : ListDeleteInteractor<DbTransaction>
