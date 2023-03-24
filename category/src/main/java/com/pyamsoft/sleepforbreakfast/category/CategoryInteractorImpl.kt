package com.pyamsoft.sleepforbreakfast.category

import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.category.CategoryChangeEvent
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDeleteDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryRealtime
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CategoryInteractorImpl
@Inject
constructor(
    private val categoryInsertDao: CategoryInsertDao,
    private val categoryDeleteDao: CategoryDeleteDao,
    private val categoryRealtime: CategoryRealtime,
    private val categoryQueryDao: CategoryQueryDao,
    private val categoryQueryCache: CategoryQueryDao.Cache,
) : CategoryInteractor, ListInteractorImpl<DbCategory.Id, DbCategory, CategoryChangeEvent>() {

  override suspend fun performQueryAll(): List<DbCategory> {
    return categoryQueryDao.query()
  }

  override suspend fun performQueryOne(id: DbCategory.Id): Maybe<out DbCategory> {
    return categoryQueryDao.queryById(id)
  }

  override suspend fun performClearCache() {
    categoryQueryCache.invalidate()
  }

  override suspend fun performClearCache(id: DbCategory.Id) {
    categoryQueryCache.invalidateById(id)
  }

  override suspend fun performListenRealtime(onEvent: (CategoryChangeEvent) -> Unit) {
    categoryRealtime.listenForChanges(onEvent)
  }

  override suspend fun performInsert(item: DbCategory): DbInsert.InsertResult<DbCategory> {
    return categoryInsertDao.insert(item)
  }

  override suspend fun performDelete(item: DbCategory): Boolean {
    return categoryDeleteDao.delete(item)
  }
}
