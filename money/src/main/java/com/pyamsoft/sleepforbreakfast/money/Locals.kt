package com.pyamsoft.sleepforbreakfast.money

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.pyamsoft.sleepforbreakfast.money.observer.CategoryObserver
import com.pyamsoft.sleepforbreakfast.money.observer.TransactionObserver

@JvmField
val LocalCategoryColor: ProvidableCompositionLocal<Color> = staticCompositionLocalOf {
  Color.Unspecified
}

@JvmField
val LocalCategoryObserver: ProvidableCompositionLocal<CategoryObserver> = staticCompositionLocalOf {
  throw IllegalStateException("LocalCategoryObserver must be provided")
}

@JvmField
val LocalTransactionObserver: ProvidableCompositionLocal<TransactionObserver> =
    staticCompositionLocalOf {
      throw IllegalStateException("LocalTransactionObserver must be provided")
    }
