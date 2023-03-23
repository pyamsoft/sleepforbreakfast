package com.pyamsoft.sleepforbreakfast.money.list

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper

interface ListDeleteInteractor<T : Any> {

  @CheckResult suspend fun delete(item: T): ResultWrapper<Boolean>
}
