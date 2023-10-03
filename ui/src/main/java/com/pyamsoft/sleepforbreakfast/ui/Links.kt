package com.pyamsoft.sleepforbreakfast.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

private inline fun AnnotatedString.Builder.withStringAnnotation(
    tag: String,
    annotation: String,
    content: () -> Unit
) {
  pushStringAnnotation(
      tag = tag,
      annotation = annotation,
  )
  content()
  pop()
}

fun AnnotatedString.Builder.appendLink(
    tag: String,
    linkColor: Color,
    text: String,
    url: String,
) {
  withStringAnnotation(
      tag = tag,
      annotation = url,
  ) {
    withStyle(
        style =
            SpanStyle(
                textDecoration = TextDecoration.Underline,
                color = linkColor,
            ),
    ) {
      append(text)
    }
  }
}
