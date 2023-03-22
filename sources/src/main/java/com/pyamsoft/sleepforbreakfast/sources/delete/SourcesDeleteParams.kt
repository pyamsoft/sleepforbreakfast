package com.pyamsoft.sleepforbreakfast.sources.delete

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.squareup.moshi.JsonClass

@Stable
data class SourcesDeleteParams(
    val sourcesId: DbSource.Id,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        sourcesId = sourcesId.raw,
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json(
      val sourcesId: String,
  ) {

    @CheckResult
    fun fromJson(): SourcesDeleteParams {
      return SourcesDeleteParams(
          sourcesId = DbSource.Id(sourcesId),
      )
    }
  }
}
