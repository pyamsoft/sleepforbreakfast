package com.pyamsoft.sleepforbreakfast.transactions.add

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.add.ListAddInteractor

internal interface TransactionAddInteractor : ListAddInteractor<DbTransaction>
