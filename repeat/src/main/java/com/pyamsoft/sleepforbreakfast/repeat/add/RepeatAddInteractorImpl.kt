package com.pyamsoft.sleepforbreakfast.repeat.add

import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.money.add.ListAddInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RepeatAddInteractorImpl
@Inject
constructor(
    private val repeatInsertDao: RepeatInsertDao,
) : RepeatAddInteractor, ListAddInteractorImpl<DbRepeat>() {

  override suspend fun performInsert(item: DbRepeat): DbInsert.InsertResult<DbRepeat> {
    return repeatInsertDao.insert(item)
  }
}
