package com.pyamsoft.sleepforbreakfast.db

import kotlinx.coroutines.CoroutineScope

fun interface TransactionHandler {

  fun runInTransaction(
      scope: CoroutineScope,
      block: CoroutineScope.() -> Unit,
  )
}
