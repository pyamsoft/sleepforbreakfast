package com.pyamsoft.sleepforbreakfast.repeat.delete

import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDeleteDao
import com.pyamsoft.sleepforbreakfast.money.delete.ListDeleteInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RepeatDeleteInteractorImpl
@Inject
constructor(
    private val repeatDeleteDao: RepeatDeleteDao,
) : RepeatDeleteInteractor, ListDeleteInteractorImpl<DbRepeat>() {

  override suspend fun performDelete(item: DbRepeat): Boolean {
    return repeatDeleteDao.delete(item)
  }
}
