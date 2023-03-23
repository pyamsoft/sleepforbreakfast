package com.pyamsoft.sleepforbreakfast.sources.delete

import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceDeleteDao
import com.pyamsoft.sleepforbreakfast.money.delete.ListDeleteInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SourcesDeleteInteractorImpl
@Inject
constructor(
    private val sourceDeleteDao: SourceDeleteDao,
) : SourcesDeleteInteractor, ListDeleteInteractorImpl<DbSource>() {

  override suspend fun performDelete(item: DbSource): Boolean {
    return sourceDeleteDao.delete(item)
  }
}
