package com.pyamsoft.sleepforbreakfast.sources.add

import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceInsertDao
import com.pyamsoft.sleepforbreakfast.money.list.ListAddInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SourceAddInteractorImpl
@Inject
constructor(
    private val sourceInsertDao: SourceInsertDao,
) : SourceAddInteractor, ListAddInteractorImpl<DbSource>() {

  override suspend fun performInsert(item: DbSource): DbInsert.InsertResult<DbSource> {
    return sourceInsertDao.insert(item)
  }
}
