package com.pyamsoft.sleepforbreakfast.repeat.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat

internal interface RepeatAddInteractor {

  @CheckResult suspend fun submit(repeat: DbRepeat): ResultWrapper<DbInsert.InsertResult<DbRepeat>>
}
