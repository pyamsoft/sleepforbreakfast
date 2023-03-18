package com.pyamsoft.sleepforbreakfast.transactions.base

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

internal interface DeleteRestoreHandler {

  fun <T : Any> handleDeleteFinal(recentlyDeleted: MutableStateFlow<T?>, onDeleted: (T) -> Unit)

  fun <T : Any> handleRestoreDeleted(
      scope: CoroutineScope,
      recentlyDeleted: MutableStateFlow<T?>,
      restore: suspend (T) -> ResultWrapper<DbInsert.InsertResult<T>>
  )
}
