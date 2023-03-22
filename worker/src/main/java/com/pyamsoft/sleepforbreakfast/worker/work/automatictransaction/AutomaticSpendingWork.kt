package com.pyamsoft.sleepforbreakfast.worker.work.automatictransaction

import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.worker.work.BgWorker
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class AutomaticSpendingWork
@Inject
internal constructor(
  private val automaticQueryDao: AutomaticQueryDao,
  private val handler: AutomaticTransactionHandler,
) : BgWorker {

  private suspend fun processJobs() {
    val unconsumed = automaticQueryDao.queryUnused()

    for (auto in unconsumed) {
      // Maybe I suck at SQL
      if (auto.used) {
        continue
      }

      handler.process(auto)
    }
  }

  override suspend fun work(): BgWorker.WorkResult =
      withContext(context = Dispatchers.IO) {
        try {
          processJobs()
          return@withContext BgWorker.WorkResult.Success
        } catch (e: Throwable) {
          if (e is CancellationException) {
            Timber.w("Job cancelled during processing")
            return@withContext BgWorker.WorkResult.Cancelled
          } else {
            Timber.e(e, "Error during processing of unconsumed automatics")
            return@withContext BgWorker.WorkResult.Failed(e)
          }
        }
      }
}
