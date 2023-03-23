package com.pyamsoft.sleepforbreakfast.repeat

import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatChangeEvent
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDeleteDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatRealtime
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RepeatInteractorImpl
@Inject
constructor(
    private val repeatQueryDao: RepeatQueryDao,
    private val repeatQueryCache: RepeatQueryDao.Cache,
    private val repeatInsertDao: RepeatInsertDao,
    private val repeatDeleteDao: RepeatDeleteDao,
    private val repeatRealtime: RepeatRealtime,
) : RepeatInteractor, ListInteractorImpl<DbRepeat.Id, DbRepeat, RepeatChangeEvent>() {

  override suspend fun performQueryAll(): List<DbRepeat> {
    return repeatQueryDao.query()
  }

  override suspend fun performQueryOne(id: DbRepeat.Id): Maybe<out DbRepeat> {
    return repeatQueryDao.queryById(id)
  }

  override suspend fun performClearCache() {
    repeatQueryCache.invalidate()
  }

  override suspend fun performClearCache(id: DbRepeat.Id) {
    repeatQueryCache.invalidateById(id)
  }

  override suspend fun performListenRealtime(onEvent: (RepeatChangeEvent) -> Unit) {
    return repeatRealtime.listenForChanges(onEvent)
  }

  override suspend fun performInsert(item: DbRepeat): DbInsert.InsertResult<DbRepeat> {
    return repeatInsertDao.insert(item)
  }

  override suspend fun performDelete(item: DbRepeat): Boolean {
    return repeatDeleteDao.delete(item)
  }
}
