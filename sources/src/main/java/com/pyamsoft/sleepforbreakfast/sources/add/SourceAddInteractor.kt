package com.pyamsoft.sleepforbreakfast.sources.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.source.DbSource

internal interface SourceAddInteractor {

  @CheckResult suspend fun submit(source: DbSource): ResultWrapper<DbInsert.InsertResult<DbSource>>
}
