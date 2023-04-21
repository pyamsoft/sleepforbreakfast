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

package com.pyamsoft.sleepforbreakfast.spending

import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.automatic.queryByAutomaticNotification
import com.pyamsoft.sleepforbreakfast.db.automatic.replaceCategories
import com.pyamsoft.sleepforbreakfast.spending.automatic.AutomaticManager
import com.pyamsoft.sleepforbreakfast.worker.WorkJobType
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

internal class SpendingTrackerHandlerImpl
@Inject
internal constructor(
    private val manager: AutomaticManager,
    private val automaticQueryDao: AutomaticQueryDao,
    private val automaticInsertDao: AutomaticInsertDao,
    private val workerQueue: WorkerQueue,
    private val clock: Clock,
) : SpendingTrackerHandler {

  private suspend fun handleProcessUnusedAutomatic(automatic: DbAutomatic) {
    val job = WorkJobType.ONESHOT_AUTOMATIC_TRANSACTION
    Timber.d("Enqueue job for processing $automatic: $job")
    workerQueue.enqueue(job)
  }

  override suspend fun processNotification(
      sbn: StatusBarNotification,
      extras: Bundle,
  ) =
      GLOBAL_LOCK.withLock {
        val automaticPayment = manager.extractPayment(sbn.packageName, extras) ?: return

        val automatic =
            DbAutomatic.create(clock)
                .notificationId(sbn.id)
                .notificationKey(sbn.key)
                .notificationGroup(sbn.groupKey)
                .notificationPackageName(sbn.packageName)
                .notificationPostTime(sbn.postTime)
                .notificationTitle(automaticPayment.title)
                .notificationMatchText(automaticPayment.text)
                .notificationAmountInCents(automaticPayment.amount)
                .notificationType(automaticPayment.type)
                .replaceCategories(automaticPayment.categories)

        when (val existing = automaticQueryDao.queryByAutomaticNotification(automatic)) {
          is Maybe.Data -> {
            Timber.w(
                "Found existing automatic notification matching parameters: ${mapOf(
                  "NEW" to automatic,
                  "EXISTING" to existing,
              )}")
          }
          is Maybe.None -> {
            when (val result = automaticInsertDao.insert(automatic)) {
              is DbInsert.InsertResult.Fail -> {
                Timber.e(result.error, "Failed to insert automatic $automatic")
              }
              is DbInsert.InsertResult.Update -> {
                Timber.d("Update existing automatic: $automatic")
              }
              is DbInsert.InsertResult.Insert -> {
                Timber.d("Inserted automatic: $automatic")
                handleProcessUnusedAutomatic(automatic)
              }
            }
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
