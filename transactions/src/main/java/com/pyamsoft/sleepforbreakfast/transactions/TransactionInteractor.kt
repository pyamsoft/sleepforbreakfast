package com.pyamsoft.sleepforbreakfast.transactions

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionChangeEvent
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractor

internal interface TransactionInteractor :
    ListInteractor<DbTransaction.Id, DbTransaction, TransactionChangeEvent>
