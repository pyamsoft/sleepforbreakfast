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

package com.pyamsoft.sleepforbreakfast.db.transaction

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import java.time.Clock
import java.time.LocalDateTime

@Stable
interface DbTransaction {

  @get:CheckResult val id: Id

  @get:CheckResult val createdAt: LocalDateTime

  @get:CheckResult val sourceId: DbSource.Id?

  @get:CheckResult val categories: List<DbCategory.Id>

  @get:CheckResult val name: String

  @get:CheckResult val amountInCents: Long

  @get:CheckResult val date: LocalDateTime

  @get:CheckResult val type: Type

  @get:CheckResult val note: String

  @get:CheckResult val repeatId: DbRepeat.Id?

  @get:CheckResult val automaticId: DbAutomatic.Id?

  @CheckResult fun sourceId(id: DbSource.Id): DbTransaction

  @CheckResult fun removeSourceId(): DbTransaction

  @CheckResult fun addCategory(id: DbCategory.Id): DbTransaction

  @CheckResult fun removeCategory(id: DbCategory.Id): DbTransaction

  @CheckResult fun clearCategories(): DbTransaction

  @CheckResult fun name(name: String): DbTransaction

  @CheckResult fun amountInCents(amountInCents: Long): DbTransaction

  @CheckResult fun date(date: LocalDateTime): DbTransaction

  @CheckResult fun type(type: Type): DbTransaction

  @CheckResult fun note(note: String): DbTransaction

  @CheckResult fun repeatId(id: DbRepeat.Id): DbTransaction

  @CheckResult fun automaticId(id: DbAutomatic.Id): DbTransaction

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
      override val sourceId: DbSource.Id? = null,
      override val categories: List<DbCategory.Id> = emptyList(),
      override val name: String = "",
      override val amountInCents: Long = 0,
      override val type: Type = Type.SPEND,
      override val note: String = "",
      override val repeatId: DbRepeat.Id? = null,
      override val automaticId: DbAutomatic.Id? = null,
  ) : DbTransaction {
    override fun sourceId(id: DbSource.Id): DbTransaction {
      return this.copy(sourceId = id)
    }

    override fun removeSourceId(): DbTransaction {
      return this.copy(sourceId = null)
    }

    override fun addCategory(id: DbCategory.Id): DbTransaction {
      return this.copy(categories = this.categories + id)
    }

    override fun removeCategory(id: DbCategory.Id): DbTransaction {
      return this.copy(categories = this.categories.filterNot { it == id })
    }

    override fun clearCategories(): DbTransaction {
      return this.copy(categories = emptyList())
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

    override fun repeatId(id: DbRepeat.Id): DbTransaction {
      return this.copy(repeatId = id)
    }

    override fun automaticId(id: DbAutomatic.Id): DbTransaction {
      return this.copy(automaticId = id)
    }
  }

  companion object {

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
fun DbTransaction.addCategory(category: DbCategory): DbTransaction {
  return this.addCategory(id = category.id)
}

@CheckResult
fun DbTransaction.removeCategory(category: DbCategory): DbTransaction {
  return this.removeCategory(id = category.id)
}

@CheckResult
fun DbTransaction.replaceCategories(categories: List<DbCategory.Id>): DbTransaction {
  var self = this.clearCategories()

  for (cat in categories) {
    self = self.addCategory(cat)
  }

  return self
}
