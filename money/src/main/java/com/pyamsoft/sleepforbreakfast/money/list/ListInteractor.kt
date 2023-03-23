package com.pyamsoft.sleepforbreakfast.money.list

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceChangeEvent
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction

interface ListInteractor<I: Any, T: Any, CE: Any> {

  @CheckResult suspend fun loadOne(id: I): ResultWrapper<T>

  @CheckResult suspend fun loadAll(force: Boolean): ResultWrapper<List<T>>

  @CheckResult suspend fun listenForItemChanges(onEvent: (CE) -> Unit)
}
