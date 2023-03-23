package com.pyamsoft.sleepforbreakfast.money.list

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert

interface ListAddInteractor<T : Any> {

  @CheckResult suspend fun submit(item: T): ResultWrapper<DbInsert.InsertResult<T>>
}
