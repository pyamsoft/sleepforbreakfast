package com.pyamsoft.sleepforbreakfast.money.list

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper

interface ListInteractor<I : Any, T : Any, CE : Any> {

  @CheckResult suspend fun loadOne(id: I): ResultWrapper<T>

  @CheckResult suspend fun loadAll(force: Boolean): ResultWrapper<List<T>>

  @CheckResult suspend fun listenForItemChanges(onEvent: (CE) -> Unit)
}
