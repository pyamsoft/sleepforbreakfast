package com.pyamsoft.sleepforbreakfast.sources.base

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.money.helper.LoadExistingHandlerImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceLoadHandler
@Inject
internal constructor(
    private val interactor: LoadSourcesInteractor,
) : LoadExistingHandlerImpl<DbSource.Id, DbSource>() {

  override fun isIdEmpty(id: DbSource.Id): Boolean {
    return id.isEmpty
  }

  override suspend fun loadData(id: DbSource.Id): ResultWrapper<DbSource> {
    return interactor.load(id)
  }
}
