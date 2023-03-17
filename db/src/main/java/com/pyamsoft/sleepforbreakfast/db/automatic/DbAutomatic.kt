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

package com.pyamsoft.sleepforbreakfast.db.automatic

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.IdGenerator

@Stable
interface DbAutomatic {

  @get:CheckResult val id: Id

  @get:CheckResult val notificationId: Int

  @get:CheckResult val notificationKey: String

  @get:CheckResult val notificationGroup: String

  @get:CheckResult val notificationPackageName: String

  @get:CheckResult val notificationPostTime: Long

  @get:CheckResult val notificationMatchText: String

  @get:CheckResult val notificationAmountInCents: Long

  @get:CheckResult val notificationTitle: String

  @CheckResult fun notificationId(id: Int): DbAutomatic

  @CheckResult fun notificationKey(key: String): DbAutomatic

  @CheckResult fun notificationGroup(group: String): DbAutomatic

  @CheckResult fun notificationPackageName(packageName: String): DbAutomatic

  @CheckResult fun notificationPostTime(time: Long): DbAutomatic

  @CheckResult fun notificationMatchText(text: String): DbAutomatic

  @CheckResult fun notificationAmountInCents(amount: Long): DbAutomatic

  @CheckResult fun notificationTitle(title: String): DbAutomatic

  data class Id(@get:CheckResult val raw: String) {

    @get:CheckResult val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }

  private data class Impl(
      override val id: Id,
      override val notificationId: Int = 0,
      override val notificationKey: String = "",
      override val notificationGroup: String = "",
      override val notificationPackageName: String = "",
      override val notificationPostTime: Long = 0,
      override val notificationAmountInCents: Long = 0,
      override val notificationMatchText: String = "",
      override val notificationTitle: String = "",
  ) : DbAutomatic {

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
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(): DbAutomatic {
      return Impl(
          id = Id(IdGenerator.generate()),
      )
    }
  }
}
