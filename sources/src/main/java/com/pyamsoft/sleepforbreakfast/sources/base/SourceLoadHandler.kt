package com.pyamsoft.sleepforbreakfast.sources.base

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.money.helper.LoadExistingHandlerImpl
import com.pyamsoft.sleepforbreakfast.sources.SourcesInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceLoadHandler
@Inject
internal constructor(
    private val interactor: SourcesInteractor,
) : LoadExistingHandlerImpl<DbSource.Id, DbSource>() {

  override fun isIdEmpty(id: DbSource.Id): Boolean {
    return id.isEmpty
  }

  override suspend fun loadData(id: DbSource.Id): ResultWrapper<DbSource> {
    return interactor.loadOne(id)
  }
}
