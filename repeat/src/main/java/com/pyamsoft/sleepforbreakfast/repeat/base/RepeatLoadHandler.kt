package com.pyamsoft.sleepforbreakfast.repeat.base

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.money.helper.LoadExistingHandlerImpl
import com.pyamsoft.sleepforbreakfast.repeat.RepeatInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepeatLoadHandler
@Inject
internal constructor(
    private val interactor: RepeatInteractor,
) : LoadExistingHandlerImpl<DbRepeat.Id, DbRepeat>() {

  override fun isIdEmpty(id: DbRepeat.Id): Boolean {
    return id.isEmpty
  }

  override suspend fun loadData(id: DbRepeat.Id): ResultWrapper<DbRepeat> {
    return interactor.loadOne(id)
  }
}
