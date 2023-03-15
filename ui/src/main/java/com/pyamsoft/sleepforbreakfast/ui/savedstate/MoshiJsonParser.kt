package com.pyamsoft.sleepforbreakfast.ui.savedstate

import com.pyamsoft.sleepforbreakfast.ui.InternalApi
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MoshiJsonParser
@Inject
constructor(
  @InternalApi private val moshi: Moshi,
) : JsonParser {

  override fun <T : Any> toJson(data: T): String {
    return moshi.adapter<T>(data::class.java).toJson(data)
  }

  override fun <T : Any> fromJson(json: String, type: Class<T>): T? {
    return moshi.adapter(type).fromJson(json)
  }
}
