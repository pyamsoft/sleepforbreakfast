package com.pyamsoft.sleepforbreakfast.core

/**
 * Easy optional data type for use with Cachify amonst other things
 *
 * Cachify expects non-null data returned, so we use this to signal "empty" results
 */
sealed class Maybe<T> {
  data class Data<T>(val data: T) : Maybe<T>()
  object None : Maybe<Nothing>()
}
