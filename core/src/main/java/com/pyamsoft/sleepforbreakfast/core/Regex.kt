package com.pyamsoft.sleepforbreakfast.core

@JvmField val REGEX_FILTER_ONLY_DIGITS = Regex("[^\\d+]")

/**
 * This claims the \\. is redundant, but a Regex checker says it is required to escape \. even in a
 * group
 */
@Suppress("RegExpRedundantEscape") const val RAW_STRING_DOLLAR_PRICE = "\\$[\\d\\.,]+"
@JvmField val REGEX_DOLLAR_PRICE = RAW_STRING_DOLLAR_PRICE.toRegex()
