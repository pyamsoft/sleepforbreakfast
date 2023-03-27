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
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
internal class RepeatTransactionHandler
@Inject
internal constructor(
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
        .type(repeat.transactionType)
        .name(repeat.transactionName)
        .note(repeat.transactionNote)
        .date(date.atTime(LocalTime.now(clock)))
        .amountInCents(repeat.transactionAmountInCents)
        .replaceCategories(repeat.transactionCategories)
  }

  private suspend fun markRepeatUsed(repeat: DbRepeat, today: LocalDate) {
    when (val res = repeatInsertDao.insert(repeat.lastRunDay(today))) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(
            res.error,
            "Failed updating Repeat to lastUsed: ${mapOf(
                      "repeat" to repeat,
                      "date" to today,
                  )}")
      }
      is DbInsert.InsertResult.Insert -> {
        Timber.d(
            "Inserted new repeat, should this happen?: ${mapOf(
                      "repeat" to repeat,
                      "date" to today,
                  )}")
      }
      is DbInsert.InsertResult.Update -> {
        Timber.d(
            "Updated existing transaction for repeat. Should this happen? ${mapOf(
                      "repeat" to repeat,
                      "transaction" to res.data,
                  )}")
      }
    }
  }

  private suspend fun insertNewTransaction(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    // Make a new transaction and put it into the table
    val transaction = createTransaction(repeat, today)

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
            "Inserted new transaction for repeat: ${mapOf(
              "repeat" to repeat,
              "transaction" to res.data,
          )}")
      }
      is DbInsert.InsertResult.Update -> {
        Timber.d(
            "Updated existing transaction for repeat. Should this happen? ${mapOf(
                  "repeat" to repeat,
                  "transaction" to res.data,
              )}")
      }
    }
  }

  private suspend fun executeRepeat(repeat: DbRepeat, today: LocalDate) = coroutineScope {
    // Launch these to run them in parallel
    launch(context = Dispatchers.Default) { insertNewTransaction(repeat, today) }
    launch(context = Dispatchers.Default) { markRepeatUsed(repeat, today) }
  }

  private suspend fun createTransactionFromDailyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    val mostRecentCreated = repeat.lastRunDay

    // If we have no recent transaction, or the recent transaction is before today,
    // we may be able to
    //
    // else
    //
    // The most recent date is the same before today, we may be able to
    val canCreate = if (mostRecentCreated == null) true else mostRecentCreated < today

    if (!canCreate) {
      Timber.d("Not asked to create new daily transaction, no good")
      return
    }

    // Make sure we have "started"
    if (today >= repeat.firstDate) {
      executeRepeat(repeat, today)
    }
  }

  private suspend fun createTransactionFromWeeklyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    val mostRecentCreated = repeat.lastRunDay

    // If we have no recent transaction, or the recent transaction is before today,
    // we may be able to
    //
    // else
    //
    // The most recent date is the same before today, we may be able to
    val canCreate = if (mostRecentCreated == null) true else mostRecentCreated < today

    if (!canCreate) {
      Timber.d("Not asked to create new weekly transaction, no good")
      return
    }

    // Make sure the day of the week matches the requested,
    // then this should be the "next" week, so make it again
    if (today >= repeat.firstDate && today.dayOfWeek == repeat.firstDate.dayOfWeek) {
      executeRepeat(repeat, today)
    }
  }

  private suspend fun createTransactionFromMonthlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    val mostRecentCreated = repeat.lastRunDay

    var canCreate = false

    // If we have no recent transaction we can create if the day matches
    if (mostRecentCreated == null) {
      canCreate = true
    } else if (mostRecentCreated < today) {
      // If the most recently created date is before today, maybe

      // The month is last month
      if (mostRecentCreated.month < today.month) {
        canCreate = true
      }
    }

    if (!canCreate) {
      Timber.d("Not asked to create new monthly transaction, no good")
      return
    }

    // Make sure the day of the month matches the requested,
    // then this should be the "next" month, so make it again
    if (today >= repeat.firstDate && today.dayOfMonth == repeat.firstDate.dayOfMonth) {
      executeRepeat(repeat, today)
    }
  }

  private suspend fun createTransactionFromYearlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    var canCreate = false

    val mostRecentCreated = repeat.lastRunDay

    // If we have no recent transaction we can create if the day matches
    if (mostRecentCreated == null) {
      canCreate = true
    } else if (mostRecentCreated < today) {
      // If the most recently created date is before today, maybe

      // The month is last month
      if (mostRecentCreated.year < today.year) {
        canCreate = true
      }
    }

    if (!canCreate) {
      Timber.d("Not asked to create new yearly transaction, no good")
      return
    }

    // Make sure the day of the year matches the requested,
    // then this should be the "next" month, so make it again
    if (today >= repeat.firstDate && today.dayOfYear == repeat.firstDate.dayOfYear) {
      executeRepeat(repeat, today)
    }
  }

  suspend fun process(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    // Just in case my SQL is bad
    if (repeat.archived) {
      Timber.w("Cannot process from archived repeat")
      return
    }

    if (!repeat.active) {
      Timber.w("Cannot process from inactive repeat")
      return
    }

    if (repeat.lastRunDay == today) {
      Timber.w("Cannot process from already used repeat today")
      return
    }

    try {
      when (repeat.repeatType) {
        DbRepeat.Type.DAILY -> createTransactionFromDailyRepeat(repeat, today)
        DbRepeat.Type.WEEKLY_ON_DAY -> createTransactionFromWeeklyRepeat(repeat, today)
        DbRepeat.Type.MONTHLY_ON_DAY -> createTransactionFromMonthlyRepeat(repeat, today)
        DbRepeat.Type.YEARLY_ON_DAY -> createTransactionFromYearlyRepeat(repeat, today)
      }
    } catch (e: Throwable) {
      Timber.e(e, "Error during creating transaction from repeat: $repeat")
    }
  }
}
