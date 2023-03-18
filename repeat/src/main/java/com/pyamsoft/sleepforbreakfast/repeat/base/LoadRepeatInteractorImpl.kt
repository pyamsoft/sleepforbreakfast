package com.pyamsoft.sleepforbreakfast.repeat.base

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class LoadRepeatInteractorImpl
@Inject
constructor(
    private val repeatQueryDao: RepeatQueryDao,
) : LoadRepeatInteractor {

  override suspend fun load(repeatId: DbRepeat.Id): ResultWrapper<DbRepeat> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          when (val repeat = repeatQueryDao.queryById(repeatId)) {
            is Maybe.Data -> {
              ResultWrapper.success(repeat.data)
            }
            is Maybe.None -> {
              val err = RuntimeException("Could not find repeat with ID $repeatId")
              Timber.w(err.message)
              ResultWrapper.failure(err)
            }
          }
        } catch (e: Throwable) {
          Timber.e(e, "Error loading repeat $repeatId")
          ResultWrapper.failure(e)
        }
      }
}
