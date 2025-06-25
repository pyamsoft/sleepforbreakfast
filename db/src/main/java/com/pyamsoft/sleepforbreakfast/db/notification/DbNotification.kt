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

  @get:CheckResult val system: Boolean

  @get:CheckResult val taintedOn: LocalDateTime?

  @get:CheckResult val type: DbTransaction.Type

  @CheckResult fun name(name: String): DbNotification

  @CheckResult fun enabled(enabled: Boolean): DbNotification

  @CheckResult fun actOnPackageName(packageNames: Collection<String>): DbNotification

  @CheckResult fun type(type: DbTransaction.Type): DbNotification

  @CheckResult fun markTaintedByUser(date: LocalDateTime): DbNotification

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
      override val system: Boolean,
      override val type: DbTransaction.Type,
      override val taintedOn: LocalDateTime?,
  ) : DbNotification {

    override fun name(name: String): DbNotification {
      return this.copy(name = name)
    }

    override fun enabled(enabled: Boolean): DbNotification {
      return this.copy(enabled = enabled)
    }

    override fun actOnPackageName(packageNames: Collection<String>): DbNotification {
      return this.copy(actOnPackageNames = packageNames)
    }

    override fun type(type: DbTransaction.Type): DbNotification {
      return this.copy(type = type)
    }

    override fun markTaintedByUser(date: LocalDateTime): DbNotification {
      return if (taintedOn == null) this.copy(taintedOn = date) else this
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
        system: Boolean = false,
        enabled: Boolean = true,
        id: Id = Id(IdGenerator.generate()),
    ): DbNotification {
      return Impl(
          id = id,
          createdAt = LocalDateTime.now(clock),
          name = name,
          actOnPackageNames = actOnPackageNames,
          type = type,
          enabled = enabled,
          system = system,
          taintedOn = null,
      )
    }
  }
}
