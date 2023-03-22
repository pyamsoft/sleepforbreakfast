package com.pyamsoft.sleepforbreakfast.sources.add

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceInsertDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class SourceAddInteractorImpl
@Inject
constructor(
    private val sourceInsertDao: SourceInsertDao,
) : SourceAddInteractor {

  override suspend fun submit(source: DbSource): ResultWrapper<DbInsert.InsertResult<DbSource>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          ResultWrapper.success(sourceInsertDao.insert(source))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error submitting source: $source")
            ResultWrapper.failure(e)
          }
        }
      }
}
