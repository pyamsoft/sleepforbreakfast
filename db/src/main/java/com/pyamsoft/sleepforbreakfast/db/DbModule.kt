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
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticDbImpl
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticDeleteDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticRealtime
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDb
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDbImpl
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDeleteDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryRealtime
import com.pyamsoft.sleepforbreakfast.db.category.system.SystemCategories
import com.pyamsoft.sleepforbreakfast.db.category.system.SystemCategoriesImpl
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDb
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDbImpl
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDeleteDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatRealtime
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDb
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDbImpl
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionRealtime
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) private annotation class InternalApi

@Module
abstract class DbModule {

  // System
  @Binds
  @CheckResult
  internal abstract fun provideSystemCategories(impl: SystemCategoriesImpl): SystemCategories

  // DB
  @Binds
  @CheckResult
  internal abstract fun bindTransactionDb(impl: TransactionDbImpl): TransactionDb

  @Binds @CheckResult internal abstract fun bindCategoryDb(impl: CategoryDbImpl): CategoryDb

  @Binds @CheckResult internal abstract fun bindRepeatDb(impl: RepeatDbImpl): RepeatDb

  @Binds @CheckResult internal abstract fun bindAutomaticDb(impl: AutomaticDbImpl): AutomaticDb

  // Caches
  @Binds
  @CheckResult
  internal abstract fun bindTransactionCache(impl: TransactionDbImpl): TransactionQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun bindCategoryCache(impl: CategoryDbImpl): CategoryQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun bindRepeatCache(impl: RepeatDbImpl): RepeatQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun bindutomaticCache(impl: AutomaticDbImpl): AutomaticQueryDao.Cache

  @Module
  companion object {

    // DbTransaction
    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideTransactionDb(db: SleepDb): TransactionDb {
      return db.transactions
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideTransactionRealtimeDao(
        @InternalApi db: TransactionDb
    ): TransactionRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideTransactionQueryDao(@InternalApi db: TransactionDb): TransactionQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideTransactionInsertDao(@InternalApi db: TransactionDb): TransactionInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideTransactionDeleteDao(@InternalApi db: TransactionDb): TransactionDeleteDao {
      return db.deleteDao
    }

    // DbCategory
    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideCategoryDb(db: SleepDb): CategoryDb {
      return db.categories
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideCategoryRealtimeDao(@InternalApi db: CategoryDb): CategoryRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideCategoryQueryDao(@InternalApi db: CategoryDb): CategoryQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideCategoryInsertDao(@InternalApi db: CategoryDb): CategoryInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideCategoryDeleteDao(@InternalApi db: CategoryDb): CategoryDeleteDao {
      return db.deleteDao
    }

    // DbRepeat
    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideRepeatDb(db: SleepDb): RepeatDb {
      return db.repeats
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideRepeatRealtimeDao(@InternalApi db: RepeatDb): RepeatRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideRepeatQueryDao(@InternalApi db: RepeatDb): RepeatQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideRepeatInsertDao(@InternalApi db: RepeatDb): RepeatInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideRepeatDeleteDao(@InternalApi db: RepeatDb): RepeatDeleteDao {
      return db.deleteDao
    }

    // DbAutomatic
    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideAutomaticDb(db: SleepDb): AutomaticDb {
      return db.automatics
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideAutomaticRealtimeDao(@InternalApi db: AutomaticDb): AutomaticRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideAutomaticQueryDao(@InternalApi db: AutomaticDb): AutomaticQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideAutomaticInsertDao(@InternalApi db: AutomaticDb): AutomaticInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideAutomaticDeleteDao(@InternalApi db: AutomaticDb): AutomaticDeleteDao {
      return db.deleteDao
    }
  }
}
