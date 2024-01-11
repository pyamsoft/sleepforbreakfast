/*
 * Copyright 2024 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.db.category

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.db.ActivateModel
import java.time.Clock
import java.time.LocalDateTime

@Stable
interface DbCategory : ActivateModel<DbCategory> {

  @get:CheckResult val id: Id

  @get:CheckResult val createdAt: LocalDateTime

  @get:CheckResult val name: String

  @get:CheckResult val note: String

  @get:CheckResult val system: Boolean

  @get:CheckResult val color: Long

  @CheckResult fun name(name: String): DbCategory

  @CheckResult fun note(note: String): DbCategory

  @CheckResult fun systemLevel(): DbCategory

  @CheckResult fun color(color: Long): DbCategory

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
      override val system: Boolean = false,
      override val active: Boolean = true,
      override val archived: Boolean = false,
      override val color: Long = 0L,
  ) : DbCategory {

    override fun name(name: String): DbCategory {
      return this.copy(name = name)
    }

    override fun note(note: String): DbCategory {
      return this.copy(note = note)
    }

    override fun systemLevel(): DbCategory {
      return this.copy(system = true)
    }

    override fun activate(): DbCategory {
      return this.copy(active = true)
    }

    override fun archive(): DbCategory {
      return this.copy(archived = true)
    }

    override fun deactivate(): DbCategory {
      return this.copy(active = false)
    }

    override fun unarchive(): DbCategory {
      return this.copy(archived = false)
    }

    override fun color(color: Long): DbCategory {
      return this.copy(color = color)
    }
  }

  companion object {

    @JvmField
    val NONE: DbCategory =
        Impl(
                id = Id.EMPTY,
                createdAt = LocalDateTime.MIN,
            )
            .name("Uncategorized")

    @JvmStatic
    @CheckResult
    fun create(
        clock: Clock,
        id: Id,
    ): DbCategory {
      return Impl(
          id = if (id.isEmpty) Id(IdGenerator.generate()) else id,
          createdAt = LocalDateTime.now(clock),
      )
    }
  }
}
