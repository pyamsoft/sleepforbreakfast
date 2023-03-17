/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.sleepforbreakfast.db.repeat

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Stable
interface DbRepeat {

  @get:CheckResult val id: Id

  @get:CheckResult val transactionSourceId: DbSource.Id?

  @get:CheckResult val transactionCategories: List<DbCategory.Id>

  @get:CheckResult val transactionName: String

  @get:CheckResult val transactionAmountInCents: Long

  @get:CheckResult val transactionType: DbTransaction.Type

  @get:CheckResult val transactionNote: String

  @get:CheckResult val firstDate: LocalDate

  @get:CheckResult val repeatDay: DayOfWeek

  @get:CheckResult val repeatTime: LocalTime

  @get:CheckResult val repeatType: Type

  @get:CheckResult val active: Boolean

  @get:CheckResult val archived: Boolean

  @CheckResult fun transactionSourceId(id: DbSource.Id): DbRepeat

  @CheckResult fun removeTransactionSourceId(): DbRepeat

  @CheckResult fun addTransactionCategory(id: DbCategory.Id): DbRepeat

  @CheckResult fun removeTransactionCategory(id: DbCategory.Id): DbRepeat

  @CheckResult fun clearTransactionCategories(): DbRepeat

  @CheckResult fun transactionName(name: String): DbRepeat

  @CheckResult fun transactionAmountInCents(amountInCents: Long): DbRepeat

  @CheckResult fun transactionType(type: DbTransaction.Type): DbRepeat

  @CheckResult fun transactionNote(note: String): DbRepeat

  @CheckResult fun repeatDay(dayOfWeek: DayOfWeek): DbRepeat

  @CheckResult fun repeatTime(time: LocalTime): DbRepeat

  @CheckResult fun repeatType(type: Type): DbRepeat

  @CheckResult fun firstDay(date: LocalDate): DbRepeat

  @CheckResult fun activate(): DbRepeat

  @CheckResult fun deactivate(): DbRepeat

  @CheckResult fun archive(): DbRepeat

  @CheckResult fun unarchive(): DbRepeat

  enum class Type {
    /** Repeats every day at the given time T */
    DAILY,

    /** Repeats each week on the given day D at given time T */
    WEEKLY_ON_DAY,

    /** Repeats each month on the given day D at given time T */
    MONTHLY_ON_DAY,

    /** Repeats each month on the given day D at given time T */
    YEARLY_ON_DAY
  }

  data class Id(@get:CheckResult val raw: String) {

    @get:CheckResult val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }

  private data class Impl(
      override val id: Id,
      override val repeatTime: LocalTime,
      override val firstDate: LocalDate,
      override val transactionSourceId: DbSource.Id? = null,
      override val transactionCategories: List<DbCategory.Id> = emptyList(),
      override val transactionName: String = "",
      override val transactionAmountInCents: Long = 0,
      override val transactionType: DbTransaction.Type = DbTransaction.Type.SPEND,
      override val transactionNote: String = "",
      override val repeatDay: DayOfWeek = DayOfWeek.MONDAY,
      override val repeatType: Type = Type.DAILY,
      override val active: Boolean = true,
      override val archived: Boolean = false,
  ) : DbRepeat {

    override fun transactionSourceId(id: DbSource.Id): DbRepeat {
      return this.copy(transactionSourceId = id)
    }

    override fun removeTransactionSourceId(): DbRepeat {
      return this.copy(transactionSourceId = null)
    }

    override fun addTransactionCategory(id: DbCategory.Id): DbRepeat {
      return this.copy(transactionCategories = this.transactionCategories + id)
    }

    override fun removeTransactionCategory(id: DbCategory.Id): DbRepeat {
      return this.copy(transactionCategories = this.transactionCategories.filterNot { it == id })
    }

    override fun clearTransactionCategories(): DbRepeat {
      return this.copy(transactionCategories = emptyList())
    }

    override fun transactionName(name: String): DbRepeat {
      return this.copy(transactionName = name)
    }

    override fun transactionAmountInCents(amountInCents: Long): DbRepeat {
      return this.copy(transactionAmountInCents = amountInCents)
    }

    override fun transactionType(type: DbTransaction.Type): DbRepeat {
      return this.copy(transactionType = type)
    }

    override fun transactionNote(note: String): DbRepeat {
      return this.copy(transactionNote = note)
    }

    override fun repeatDay(dayOfWeek: DayOfWeek): DbRepeat {
      return this.copy(repeatDay = dayOfWeek)
    }

    override fun repeatTime(time: LocalTime): DbRepeat {
      return this.copy(repeatTime = time)
    }

    override fun repeatType(type: Type): DbRepeat {
      return this.copy(repeatType = type)
    }

    override fun firstDay(date: LocalDate): DbRepeat {
      return this.copy(firstDate = date)
    }

    override fun activate(): DbRepeat {
      return this.copy(active = true)
    }

    override fun deactivate(): DbRepeat {
      return this.copy(active = false)
    }

    override fun archive(): DbRepeat {
      return this.copy(archived = true)
    }

    override fun unarchive(): DbRepeat {
      return this.copy(archived = false)
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        clock: Clock,
        id: Id,
    ): DbRepeat {
      return Impl(
          id = if (id.isEmpty) DbRepeat.Id(IdGenerator.generate()) else id,
          repeatTime = LocalTime.now(clock),
          firstDate = LocalDate.now(clock),
      )
    }
  }
}

@CheckResult
fun DbRepeat.addTransactionCategory(category: DbCategory): DbRepeat {
  return this.addTransactionCategory(id = category.id)
}

@CheckResult
fun DbRepeat.removeTransactionCategory(category: DbCategory): DbRepeat {
  return this.removeTransactionCategory(id = category.id)
}

@CheckResult
fun DbRepeat.replaceTransactionCategories(categories: List<DbCategory.Id>): DbRepeat {
  var self = this.clearTransactionCategories()

  for (cat in categories) {
    self = self.addTransactionCategory(cat)
  }

  return self
}