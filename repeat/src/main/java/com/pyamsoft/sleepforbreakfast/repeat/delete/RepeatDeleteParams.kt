package com.pyamsoft.sleepforbreakfast.repeat.delete

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.squareup.moshi.JsonClass

@Stable
data class RepeatDeleteParams(
    val repeatId: DbRepeat.Id,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        repeatId = repeatId.raw,
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json(
      val repeatId: String,
  ) {

    @CheckResult
    fun fromJson(): RepeatDeleteParams {
      return RepeatDeleteParams(
          repeatId = DbRepeat.Id(repeatId),
      )
    }
  }
}
