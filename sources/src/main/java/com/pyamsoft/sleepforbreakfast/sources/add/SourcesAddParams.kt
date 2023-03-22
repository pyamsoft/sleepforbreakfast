package com.pyamsoft.sleepforbreakfast.sources.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.squareup.moshi.JsonClass

@Stable
data class SourcesAddParams(
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
    fun fromJson(): SourcesAddParams {
      return SourcesAddParams(
          sourcesId = DbSource.Id(sourcesId),
      )
    }
  }
}
