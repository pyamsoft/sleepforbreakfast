package com.pyamsoft.sleepforbreakfast.sources.base

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceQueryDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class LoadSourcesInteractorImpl
@Inject
constructor(
    private val sourceQueryDao: SourceQueryDao,
) : LoadSourcesInteractor {

  override suspend fun load(sourceId: DbSource.Id): ResultWrapper<DbSource> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          when (val repeat = sourceQueryDao.queryById(sourceId)) {
            is Maybe.Data -> {
              ResultWrapper.success(repeat.data)
            }
            is Maybe.None -> {
              val err = RuntimeException("Could not find source with ID $sourceId")
              Timber.w(err.message)
              ResultWrapper.failure(err)
            }
          }
        } catch (e: Throwable) {
          Timber.e(e, "Error loading source $sourceId")
          ResultWrapper.failure(e)
        }
      }
}
