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

package com.pyamsoft.sleepforbreakfast.db.notification

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.Clock
import java.time.LocalDateTime

@Stable
interface DbNotification {

  @get:CheckResult val id: Id

  @get:CheckResult val createdAt: LocalDateTime

  @get:CheckResult val name: String

  @get:CheckResult val actOnPackageNames: Collection<String>

  @get:CheckResult val enabled: Boolean

  @get:CheckResult val isUntouchedFromSystem: Boolean

  @get:CheckResult val type: DbTransaction.Type

  @CheckResult fun name(name: String): DbNotification

  @CheckResult fun enable(): DbNotification

  @CheckResult fun disable(): DbNotification

  @CheckResult fun addActOnPackageName(packageName: String): DbNotification

  @CheckResult fun removeActOnPackageName(packageName: String): DbNotification

  @CheckResult fun markSpend(): DbNotification

  @CheckResult fun markEarn(): DbNotification

  @CheckResult fun markTaintedByUser(): DbNotification

  data class Id(@get:CheckResult val raw: String) {

    @get:CheckResult val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }

  private data class Impl(
      override val id: Id,
      override val createdAt: LocalDateTime,
      override val name: String,
      override val actOnPackageNames: Collection<String>,
      override val enabled: Boolean,
      override val type: DbTransaction.Type,
      override val isUntouchedFromSystem: Boolean,
  ) : DbNotification {

    override fun name(name: String): DbNotification {
      return this.copy(name = name)
    }

    override fun enable(): DbNotification {
      return this.copy(enabled = true)
    }

    override fun disable(): DbNotification {
      return this.copy(enabled = false)
    }

    override fun addActOnPackageName(packageName: String): DbNotification {
      return this.copy(
          actOnPackageNames = this.actOnPackageNames + packageName,
      )
    }

    override fun removeActOnPackageName(packageName: String): DbNotification {
      return this.copy(
          actOnPackageNames = this.actOnPackageNames.filterNot { it == packageName },
      )
    }

    override fun markEarn(): DbNotification {
      return this.copy(type = DbTransaction.Type.EARN)
    }

    override fun markSpend(): DbNotification {
      return this.copy(type = DbTransaction.Type.SPEND)
    }

    override fun markTaintedByUser(): DbNotification {
      return this.copy(isUntouchedFromSystem = false)
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    @JvmOverloads
    fun create(
        clock: Clock,
        actOnPackageNames: Collection<String>,
        type: DbTransaction.Type,
        name: String,
        enabled: Boolean = true,
        isUntouchedFromSystem: Boolean = true,
        id: Id = Id(IdGenerator.generate()),
    ): DbNotification {
      return Impl(
          id = id,
          createdAt = LocalDateTime.now(clock),
          name = name,
          actOnPackageNames = actOnPackageNames,
          type = type,
          isUntouchedFromSystem = isUntouchedFromSystem,
          enabled = enabled,
      )
    }
  }
}
