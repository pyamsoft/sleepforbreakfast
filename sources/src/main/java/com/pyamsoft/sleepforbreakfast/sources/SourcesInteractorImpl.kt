package com.pyamsoft.sleepforbreakfast.sources

import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceChangeEvent
import com.pyamsoft.sleepforbreakfast.db.source.SourceDeleteDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceInsertDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceQueryDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceRealtime
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SourcesInteractorImpl
@Inject
constructor(
    private val sourceInsertDao: SourceInsertDao,
    private val sourceDeleteDao: SourceDeleteDao,
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

  override suspend fun performClearCache(id: DbSource.Id) {
    sourceQueryCache.invalidateById(id)
  }

  override suspend fun performListenRealtime(onEvent: (SourceChangeEvent) -> Unit) {
    sourceRealtime.listenForChanges(onEvent)
  }

  override suspend fun performInsert(item: DbSource): DbInsert.InsertResult<DbSource> {
    return sourceInsertDao.insert(item)
  }

  override suspend fun performDelete(item: DbSource): Boolean {
    return sourceDeleteDao.delete(item)
  }
}
