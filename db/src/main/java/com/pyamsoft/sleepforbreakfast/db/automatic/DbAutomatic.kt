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

package com.pyamsoft.sleepforbreakfast.db.automatic

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.Clock
import java.time.LocalDateTime

@Stable
interface DbAutomatic {

  @get:CheckResult val id: Id

  @get:CheckResult val createdAt: LocalDateTime

  @get:CheckResult val categories: List<DbCategory.Id>

  @get:CheckResult val notificationId: Int

  @get:CheckResult val notificationKey: String

  @get:CheckResult val notificationGroup: String

  @get:CheckResult val notificationPackageName: String

  @get:CheckResult val notificationPostTime: Long

  @get:CheckResult val notificationMatchText: String

  @get:CheckResult val notificationAmountInCents: Long

  @get:CheckResult val notificationTitle: String

  @get:CheckResult val notificationType: DbTransaction.Type

  // Optional
  @get:CheckResult val notificationOptionalAccount: String
  @get:CheckResult val notificationOptionalDate: String
  @get:CheckResult val notificationOptionalMerchant: String
  @get:CheckResult val notificationOptionalDescription: String

  @get:CheckResult val used: Boolean

  @CheckResult fun addCategory(id: DbCategory.Id): DbAutomatic

  @CheckResult fun removeCategory(id: DbCategory.Id): DbAutomatic

  @CheckResult fun clearCategories(): DbAutomatic

  @CheckResult fun notificationId(id: Int): DbAutomatic

  @CheckResult fun notificationKey(key: String): DbAutomatic

  @CheckResult fun notificationGroup(group: String): DbAutomatic

  @CheckResult fun notificationPackageName(packageName: String): DbAutomatic

  @CheckResult fun notificationPostTime(time: Long): DbAutomatic

  @CheckResult fun notificationMatchText(text: String): DbAutomatic

  @CheckResult fun notificationAmountInCents(amount: Long): DbAutomatic

  @CheckResult fun notificationTitle(title: String): DbAutomatic

  @CheckResult fun notificationType(type: DbTransaction.Type): DbAutomatic

  @CheckResult fun consume(): DbAutomatic

  // Optional
  @CheckResult fun notificationOptionalAccount(optional: String): DbAutomatic

  @CheckResult fun notificationOptionalDate(optional: String): DbAutomatic

  @CheckResult fun notificationOptionalMerchant(optional: String): DbAutomatic

  @CheckResult fun notificationOptionalDescription(optional: String): DbAutomatic

  data class Id(@get:CheckResult val raw: String) {

    @get:CheckResult val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }

  private data class Impl(
      override val id: Id,
      override val createdAt: LocalDateTime,
      override val categories: List<DbCategory.Id> = emptyList(),
      override val notificationId: Int = 0,
      override val notificationKey: String = "",
      override val notificationGroup: String = "",
      override val notificationPackageName: String = "",
      override val notificationPostTime: Long = 0,
      override val notificationAmountInCents: Long = 0,
      override val notificationMatchText: String = "",
      override val notificationTitle: String = "",
      override val notificationType: DbTransaction.Type = DbTransaction.Type.SPEND,
      override val used: Boolean = false,

      // Optional
      override val notificationOptionalAccount: String = "",
      override val notificationOptionalDate: String = "",
      override val notificationOptionalMerchant: String = "",
      override val notificationOptionalDescription: String = "",
  ) : DbAutomatic {

    override fun addCategory(id: DbCategory.Id): DbAutomatic {
      return this.copy(categories = this.categories + id)
    }

    override fun removeCategory(id: DbCategory.Id): DbAutomatic {
      return this.copy(categories = this.categories.filterNot { it == id })
    }

    override fun clearCategories(): DbAutomatic {
      return this.copy(categories = emptyList())
    }

    override fun notificationId(id: Int): DbAutomatic {
      return this.copy(notificationId = id)
    }

    override fun notificationKey(key: String): DbAutomatic {
      return this.copy(notificationKey = key)
    }

    override fun notificationGroup(group: String): DbAutomatic {
      return this.copy(notificationGroup = group)
    }

    override fun notificationPackageName(packageName: String): DbAutomatic {
      return this.copy(notificationPackageName = packageName)
    }

    override fun notificationPostTime(time: Long): DbAutomatic {
      return this.copy(notificationPostTime = time)
    }

    override fun notificationMatchText(text: String): DbAutomatic {
      return this.copy(notificationMatchText = text)
    }

    override fun notificationAmountInCents(amount: Long): DbAutomatic {
      return this.copy(notificationAmountInCents = amount)
    }

    override fun notificationTitle(title: String): DbAutomatic {
      return this.copy(notificationTitle = title)
    }

    override fun notificationType(type: DbTransaction.Type): DbAutomatic {
      return this.copy(notificationType = type)
    }

    override fun consume(): DbAutomatic {
      return this.copy(used = true)
    }

    // Optional
    override fun notificationOptionalAccount(optional: String): DbAutomatic {
      return this.copy(notificationOptionalAccount = optional)
    }

    override fun notificationOptionalDate(optional: String): DbAutomatic {
      return this.copy(notificationOptionalDate = optional)
    }

    override fun notificationOptionalMerchant(optional: String): DbAutomatic {
      return this.copy(notificationOptionalMerchant = optional)
    }

    override fun notificationOptionalDescription(optional: String): DbAutomatic {
      return this.copy(notificationOptionalDescription = optional)
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(clock: Clock): DbAutomatic {
      return Impl(
          id = Id(IdGenerator.generate()),
          createdAt = LocalDateTime.now(clock),
      )
    }
  }
}

@CheckResult
fun DbAutomatic.replaceCategories(categories: Collection<DbCategory.Id>): DbAutomatic {
  var self = this.clearCategories()

  for (cat in categories) {
    self = self.addCategory(cat)
  }

  return self
}
