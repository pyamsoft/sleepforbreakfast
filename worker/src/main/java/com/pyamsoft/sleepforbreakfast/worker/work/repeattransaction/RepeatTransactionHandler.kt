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

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

internal class RepeatTransactionHandler
@Inject
internal constructor(
    private val repeatQueryDao: RepeatQueryDao,
    private val repeatInsertDao: RepeatInsertDao,
    private val transactionQueryDao: TransactionQueryDao,
    private val transactionInsertDao: TransactionInsertDao,
    private val clock: Clock,
) {

  @CheckResult
  private fun createTransaction(
      repeat: DbRepeat,
      date: LocalDate,
  ): DbTransaction {
    return DbTransaction.create(clock, DbTransaction.Id.EMPTY)
        // Link to this Repeat instance
        .repeatId(repeat.id)
        .repeatCreatedDate(date)
        .type(repeat.transactionType)
        .name(repeat.transactionName)
        .note(repeat.transactionNote)
        .date(date.atTime(LocalTime.now(clock)))
        .amountInCents(repeat.transactionAmountInCents)
        .replaceCategories(repeat.transactionCategories)
  }

  private suspend fun markRepeatUsed(repeat: DbRepeat, date: LocalDate) {
    when (val res = repeatInsertDao.insert(repeat.lastRunDay(date))) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(
            res.error,
            "Failed updating Repeat to lastUsed: ${mapOf(
                      "repeat" to repeat,
                      "date" to date,
                  )}")
      }
      is DbInsert.InsertResult.Insert -> {
        Timber.d(
            "Inserted new repeat, should this happen?: ${mapOf(
                      "repeat" to repeat,
                      "date" to date,
                  )}")
      }
      is DbInsert.InsertResult.Update -> {
        Timber.d(
            "Updated existing transaction for repeat ${mapOf(
                      "repeat" to repeat,
                      "transaction" to res.data,
                  )}")
      }
    }
  }

  private suspend fun insertNewTransaction(
      repeat: DbRepeat,
      date: LocalDate,
  ) {
    // Make a new transaction and put it into the table
    val transaction =
        createTransaction(
            repeat = repeat,
            date = date,
        )

    when (val res = transactionInsertDao.insert(transaction)) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(
            res.error,
            "Failed inserting new Transaction made by repeat: ${mapOf(
                    "repeat" to repeat,
                    "transaction" to transaction,
                )}")
      }
      is DbInsert.InsertResult.Insert -> {
        Timber.d(
            "Inserted new transaction made by repeat: ${mapOf(
              "repeat" to repeat,
              "transaction" to res.data,
          )}")
      }
      is DbInsert.InsertResult.Update -> {
        Timber.d(
            "Updated existing transaction made by repeat. Should this happen? ${mapOf(
                  "repeat" to repeat,
                  "transaction" to res.data,
              )}")
      }
    }
  }

  private suspend fun executeRepeat(
      repeat: DbRepeat,
      dates: Collection<LocalDate>,
      lastUsed: LocalDate,
  ) = coroutineScope {
    val jobs =
        mutableListOf<Deferred<*>>(
            async(context = Dispatchers.Default) { markRepeatUsed(repeat, lastUsed) },
        )

    for (d in dates) {
      jobs.add(
          async(context = Dispatchers.Default) { insertNewTransaction(repeat, d) },
      )
    }

    jobs.awaitAll()
  }

  @CheckResult
  private suspend fun createTransactionFromDailyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): Collection<LocalDate> =
      createTransactionsList(
          repeat = repeat,
          today = today,
      ) {
        it.plusDays(1)
      }

  @CheckResult
  private suspend fun createTransactionFromWeeklyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): Collection<LocalDate> =
      createTransactionsList(
          repeat = repeat,
          today = today,
      ) {
        it.plusWeeks(1)
      }

  @CheckResult
  private suspend fun createTransactionFromMonthlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): Collection<LocalDate> =
      createTransactionsList(
          repeat = repeat,
          today = today,
      ) {
        it.plusMonths(1)
      }

  @CheckResult
  private suspend fun createTransactionFromYearlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): Collection<LocalDate> =
      createTransactionsList(
          repeat = repeat,
          today = today,
      ) {
        it.plusYears(1)
      }

  @CheckResult
  private suspend inline fun createTransactionsList(
      repeat: DbRepeat,
      today: LocalDate,
      adjustCursor: (LocalDate) -> LocalDate,
  ): Collection<LocalDate> {
    // We start from the last date used, or the programmed first repeat date
    var cursorDate = repeat.firstDay

    // If we have no recent transaction we can create if the day matches
    val possibleDates = mutableSetOf<LocalDate>()

    // pass repeat.firstDay here to canCreate instead of cursorDate
    // because if the cursorDate is the lastRunDay, it can be any date,
    // but we expect to run based on the "template" set by the firstDay
    // (day or week or month or year)
    while (cursorDate <= today) {
      possibleDates.add(cursorDate)
      cursorDate = adjustCursor(cursorDate)
    }

    val existingTransactions =
        transactionQueryDao.queryByRepeatOnDates(
            id = repeat.id,
            dates = possibleDates,
        )

    Timber.d("Existing transactions: $repeat $existingTransactions")
    val emptyDates = mutableSetOf<LocalDate>()
    for (d in possibleDates) {
      val existing =
          existingTransactions.firstOrNull {
            it.repeatCreatedDate != null && it.repeatCreatedDate == d
          }
      if (existing == null) {
        Timber.d("No transaction exists yet for $d")
        emptyDates.add(d)
      }
    }

    Timber.d("New transactions: $repeat $emptyDates")

    return emptyDates
  }

  suspend fun process(
      repeatId: DbRepeat.Id,
      today: LocalDate,
  ) =
      GLOBAL_LOCK.withLock {
        // Query here in case a previous DB operation caused this to be used
        when (val res = repeatQueryDao.queryById(repeatId)) {
          is Maybe.None -> {
            Timber.w("Could not find repeat for id: $repeatId")
          }
          is Maybe.Data -> {
            val repeat = res.data

            // Just in case my SQL is bad
            if (repeat.archived) {
              Timber.w("Cannot process from archived repeat")
              return@withLock
            }

            if (!repeat.active) {
              Timber.w("Cannot process from inactive repeat")
              return@withLock
            }

            if (repeat.lastRunDay == today) {
              Timber.w("Cannot process from already used repeat today")
              return@withLock
            }

            if (today < repeat.firstDay) {
              Timber.w(
                  "Cannot process repeat not started yet: ${mapOf(
                          "firstDay" to repeat.firstDay,
                          "today" to today
                      )}")
              return@withLock
            }

            // Lock a global mutex so that DB operations only happen once
            try {
              val dates =
                  when (repeat.repeatType) {
                    DbRepeat.Type.DAILY -> createTransactionFromDailyRepeat(repeat, today)
                    DbRepeat.Type.WEEKLY_ON_DAY -> createTransactionFromWeeklyRepeat(repeat, today)
                    DbRepeat.Type.MONTHLY_ON_DAY ->
                        createTransactionFromMonthlyRepeat(repeat, today)
                    DbRepeat.Type.YEARLY_ON_DAY -> createTransactionFromYearlyRepeat(repeat, today)
                  }

              if (dates.isNotEmpty()) {
                executeRepeat(
                    repeat = repeat,
                    dates = dates,
                    lastUsed = today,
                )
              } else {
                Timber.w("Not using repeat: $repeat")
              }
            } catch (e: Throwable) {
              Timber.e(e, "Error during creating transaction from repeat: $repeat")
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
