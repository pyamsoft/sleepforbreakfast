package com.pyamsoft.sleepforbreakfast.sources

import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceChangeEvent
import com.pyamsoft.sleepforbreakfast.db.source.SourceQueryDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceRealtime
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SourcesInteractorImpl
@Inject
constructor(
    private val sourceRealtime: SourceRealtime,
    private val sourceQueryDao: SourceQueryDao,
    private val sourceQueryCache: SourceQueryDao.Cache,
) : SourcesInteractor, ListInteractorImpl<DbSource.Id, DbSource, SourceChangeEvent>() {

  override suspend fun performQueryAll(): List<DbSource> {
    return sourceQueryDao.query()
  }

  override suspend fun performQueryOne(id: DbSource.Id): Maybe<out DbSource> {
    return sourceQueryDao.queryById(id)
  }

  override suspend fun performClearCache() {
    sourceQueryCache.invalidate()
  }

  override suspend fun performListenRealtime(onEvent: (SourceChangeEvent) -> Unit) {
    sourceRealtime.listenForChanges(onEvent)
  }
}
