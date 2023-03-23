package com.pyamsoft.sleepforbreakfast.money.list

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert

interface ListInteractor<I : Any, T : Any, CE : Any> {

  @CheckResult suspend fun loadOne(force: Boolean, id: I): ResultWrapper<T>

  @CheckResult suspend fun loadAll(force: Boolean): ResultWrapper<List<T>>

  @CheckResult suspend fun listenForItemChanges(onEvent: (CE) -> Unit)

  @CheckResult suspend fun submit(item: T): ResultWrapper<DbInsert.InsertResult<T>>

  @CheckResult suspend fun delete(item: T): ResultWrapper<Boolean>
}
