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

package com.pyamsoft.sleepforbreakfast.db.source

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import java.time.Clock
import java.time.LocalDateTime

@Stable
interface DbSource {

  @get:CheckResult val id: Id

  @get:CheckResult val createdAt: LocalDateTime

  @get:CheckResult val name: String

  @get:CheckResult val note: String

  @get:CheckResult val accountNumber: String

  @CheckResult fun name(name: String): DbSource

  @CheckResult fun note(note: String): DbSource

  @CheckResult fun accountNumber(accountNumber: String): DbSource

  data class Id(@get:CheckResult val raw: String) {

    @get:CheckResult val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }

  private data class Impl(
      override val id: Id,
      override val createdAt: LocalDateTime,
      override val name: String = "",
      override val note: String = "",
      override val accountNumber: String = ""
  ) : DbSource {

    override fun name(name: String): DbSource {
      return this.copy(name = name)
    }

    override fun note(note: String): DbSource {
      return this.copy(note = note)
    }

    override fun accountNumber(accountNumber: String): DbSource {
      return this.copy(accountNumber = accountNumber)
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        clock: Clock,
        id: Id,
    ): DbSource {
      return Impl(
          id = if (id.isEmpty) Id(IdGenerator.generate()) else id,
          createdAt = LocalDateTime.now(clock),
      )
    }
  }
}
