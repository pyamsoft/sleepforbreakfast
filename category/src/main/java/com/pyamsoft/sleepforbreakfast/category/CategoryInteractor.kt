package com.pyamsoft.sleepforbreakfast.category

import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.category.CategoryChangeEvent
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractor

internal interface CategoryInteractor : ListInteractor<DbCategory.Id, DbCategory, CategoryChangeEvent>
