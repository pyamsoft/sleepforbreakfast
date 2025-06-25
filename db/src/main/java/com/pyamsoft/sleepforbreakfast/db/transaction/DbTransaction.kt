/*
 * Copyright 2025 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.db.transaction

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Stable
interface DbTransaction {

  @get:CheckResult val id: Id

  @get:CheckResult val createdAt: LocalDateTime

  @get:CheckResult val categories: List<DbCategory.Id>

  @get:CheckResult val name: String

  @get:CheckResult val amountInCents: Long

  @get:CheckResult val date: LocalDateTime

  @get:CheckResult val type: Type

  @get:CheckResult val note: String

  @get:CheckResult val automaticId: DbAutomatic.Id?

  @get:CheckResult val automaticCreatedDate: LocalDate?

  @CheckResult fun addCategory(id: DbCategory.Id): DbTransaction

  @CheckResult fun removeCategory(id: DbCategory.Id): DbTransaction

  @CheckResult fun clearCategories(): DbTransaction

  @CheckResult fun name(name: String): DbTransaction

  @CheckResult fun amountInCents(amountInCents: Long): DbTransaction

  @CheckResult fun date(date: LocalDateTime): DbTransaction

  @CheckResult fun type(type: Type): DbTransaction

  @CheckResult fun note(note: String): DbTransaction

  @CheckResult fun automaticId(id: DbAutomatic.Id): DbTransaction

  @CheckResult fun automaticCreatedDate(date: LocalDate): DbTransaction

  enum class Type {
    SPEND,
    EARN
  }

  data class Id(@get:CheckResult val raw: String) {

    @get:CheckResult val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }

  private data class Impl(
      override val id: Id,
      override val createdAt: LocalDateTime,
      override val date: LocalDateTime,
      private val realCategories: List<DbCategory.Id> = emptyList(),
      override val name: String = "",
      override val amountInCents: Long = 0,
      override val type: Type = Type.SPEND,
      override val note: String = "",
      override val automaticId: DbAutomatic.Id? = null,
      override val automaticCreatedDate: LocalDate? = null,
  ) : DbTransaction {

    // Sometimes the "empty" can show up in this list, filter it out
    override val categories by lazy { realCategories.filterNot { it.isEmpty } }

    override fun addCategory(id: DbCategory.Id): DbTransaction {
      return this.copy(realCategories = this.categories + id)
    }

    override fun removeCategory(id: DbCategory.Id): DbTransaction {
      return this.copy(realCategories = this.categories.filterNot { it == id })
    }

    override fun clearCategories(): DbTransaction {
      return this.copy(realCategories = emptyList())
    }

    override fun name(name: String): DbTransaction {
      return this.copy(name = name)
    }

    override fun amountInCents(amountInCents: Long): DbTransaction {
      return this.copy(amountInCents = amountInCents)
    }

    override fun date(date: LocalDateTime): DbTransaction {
      return this.copy(date = date)
    }

    override fun type(type: Type): DbTransaction {
      return this.copy(type = type)
    }

    override fun note(note: String): DbTransaction {
      return this.copy(note = note)
    }

    override fun automaticId(id: DbAutomatic.Id): DbTransaction {
      return this.copy(automaticId = id)
    }

    override fun automaticCreatedDate(date: LocalDate): DbTransaction {
      return this.copy(automaticCreatedDate = date)
    }
  }

  companion object {
    @JvmField
    val NONE: DbTransaction =
        Impl(
            id = Id.EMPTY,
            createdAt = LocalDateTime.MIN,
            date = LocalDateTime.MIN,
        )

    @JvmStatic
    @CheckResult
    fun create(
        clock: Clock,
        id: Id,
    ): DbTransaction {
      return Impl(
          id = if (id.isEmpty) Id(IdGenerator.generate()) else id,
          createdAt = LocalDateTime.now(clock),
          date = LocalDateTime.now(clock),
      )
    }
  }
}

@CheckResult
fun DbTransaction.replaceCategories(categories: List<DbCategory.Id>): DbTransaction {
  var self = this.clearCategories()

  for (cat in categories) {
    self = self.addCategory(cat)
  }

  return self
}
