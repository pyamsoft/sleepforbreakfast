package com.pyamsoft.sleepforbreakfast.category.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.squareup.moshi.JsonClass

@Stable
data class CategoryAddParams(
    val categoryId: DbCategory.Id,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        categoryId = categoryId.raw,
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json(
      val categoryId: String,
  ) {

    @CheckResult
    fun fromJson(): CategoryAddParams {
      return CategoryAddParams(
          categoryId = DbCategory.Id(categoryId),
      )
    }
  }
}
