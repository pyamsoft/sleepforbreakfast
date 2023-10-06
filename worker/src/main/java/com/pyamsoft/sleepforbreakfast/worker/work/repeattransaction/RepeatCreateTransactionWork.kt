/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.worker.work.repeattransaction

import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.worker.work.BgWorker
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RepeatCreateTransactionWork
@Inject
internal constructor(
    private val repeatQueryDao: RepeatQueryDao,
    private val clock: Clock,
    private val handler: RepeatTransactionHandler,
) : BgWorker {

  private suspend fun processRepeats() =
      GLOBAL_LOCK.withLock {
        val today = LocalDate.now(clock)

        // Get all active repeats
        val allRepeats = repeatQueryDao.queryActive()

        for (rep in allRepeats) {
          // Pass just the ID instead of the full object
          // Because this operation may mutate a repeat object, we must be sure
          // that the repeat object has not been used in between the time when it is
          // retrieved above in a list and the time it is processed
          handler.process(rep.id, today)
        }
      }

  override suspend fun work(): BgWorker.WorkResult =
      withContext(context = Dispatchers.Default) {
        try {
          processRepeats()
          return@withContext BgWorker.WorkResult.Success
        } catch (e: Throwable) {
          if (e is CancellationException) {
            Timber.w { "Job cancelled during processing" }
            return@withContext BgWorker.WorkResult.Cancelled
          } else {
            Timber.e(e) { "Error during processing of repeats to create transactions" }
            return@withContext BgWorker.WorkResult.Failed(e)
          }
        }
      }

  companion object {

    /**
     * the global lock prevents multiple callers from running this handler at the same time as it
     * could cause duplicates in the DB if operations are close enough
     */
    private val GLOBAL_LOCK = Mutex()
  }
}
