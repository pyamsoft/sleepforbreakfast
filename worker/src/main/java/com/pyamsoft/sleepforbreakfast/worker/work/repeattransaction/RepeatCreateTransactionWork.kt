package com.pyamsoft.sleepforbreakfast.worker.work.repeattransaction

import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.worker.work.BgWorker
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class RepeatCreateTransactionWork
@Inject
internal constructor(
    private val repeatQueryDao: RepeatQueryDao,
    private val clock: Clock,
    private val handler: RepeatTransactionHandler,
) : BgWorker {

  private suspend fun processRepeats() {
    val today = LocalDate.now(clock)

    // Get all active repeats
    val allRepeats = repeatQueryDao.queryActive()

    for (rep in allRepeats) {

      // Don't do anything if we haven't "started" yet
      if (rep.firstDate < today) {
        continue
      }

      handler.process(rep, today)
    }
  }

  override suspend fun work(): BgWorker.WorkResult =
      withContext(context = Dispatchers.IO) {
        try {
          processRepeats()
          return@withContext BgWorker.WorkResult.Success
        } catch (e: Throwable) {
          if (e is CancellationException) {
            Timber.w("Job cancelled during processing")
            return@withContext BgWorker.WorkResult.Cancelled
          } else {
            Timber.e(e, "Error during processing of repeats to create transactions")
            return@withContext BgWorker.WorkResult.Failed(e)
          }
        }
      }
}
