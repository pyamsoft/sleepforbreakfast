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

package com.pyamsoft.sleepforbreakfast.db

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticDb
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDb
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDb
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDb

interface SleepDb {

  @get:CheckResult val transactions: TransactionDb

  @get:CheckResult val categories: CategoryDb

  @get:CheckResult val repeats: RepeatDb

  @get:CheckResult val automatics: AutomaticDb

  suspend fun invalidate()
}
