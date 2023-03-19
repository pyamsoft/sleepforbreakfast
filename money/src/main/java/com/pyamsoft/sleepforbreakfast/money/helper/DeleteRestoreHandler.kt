package com.pyamsoft.sleepforbreakfast.money.helper

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

interface DeleteRestoreHandler<T : Any> {

  fun handleDeleteFinal(recentlyDeleted: MutableStateFlow<T?>, onDeleted: (T) -> Unit)

  fun handleRestoreDeleted(
      scope: CoroutineScope,
      recentlyDeleted: MutableStateFlow<T?>,
      restore: suspend (T) -> ResultWrapper<DbInsert.InsertResult<T>>
  )
}
