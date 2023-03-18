package com.pyamsoft.sleepforbreakfast.repeat.add

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class RepeatAddInteractorImpl
@Inject
constructor(
    private val repeatInsertDao: RepeatInsertDao,
) : RepeatAddInteractor {

  override suspend fun submit(repeat: DbRepeat): ResultWrapper<DbInsert.InsertResult<DbRepeat>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          ResultWrapper.success(repeatInsertDao.insert(repeat))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error submitting repeat: $repeat")
            ResultWrapper.failure(e)
          }
        }
      }
}
