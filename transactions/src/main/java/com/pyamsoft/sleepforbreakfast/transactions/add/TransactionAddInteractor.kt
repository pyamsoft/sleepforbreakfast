package com.pyamsoft.sleepforbreakfast.transactions.add

import com.pyamsoft.sleepforbreakfast.transactions.base.CreateTransactionInteractor
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleTransactionInteractor

internal interface TransactionAddInteractor :
    SingleTransactionInteractor, CreateTransactionInteractor
