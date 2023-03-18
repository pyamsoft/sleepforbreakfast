package com.pyamsoft.sleepforbreakfast.repeat.base

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat

internal interface LoadRepeatInteractor {

  @CheckResult suspend fun load(repeatId: DbRepeat.Id): ResultWrapper<DbRepeat>
}
