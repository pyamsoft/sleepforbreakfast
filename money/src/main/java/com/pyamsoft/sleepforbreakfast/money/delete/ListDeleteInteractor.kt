package com.pyamsoft.sleepforbreakfast.money.delete

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper

interface ListDeleteInteractor<T : Any> {

  @CheckResult suspend fun delete(item: T): ResultWrapper<Boolean>
}
