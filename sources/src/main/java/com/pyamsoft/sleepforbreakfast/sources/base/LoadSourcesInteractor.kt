package com.pyamsoft.sleepforbreakfast.sources.base

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.source.DbSource

internal interface LoadSourcesInteractor {

  @CheckResult suspend fun load(sourceId: DbSource.Id): ResultWrapper<DbSource>
}
