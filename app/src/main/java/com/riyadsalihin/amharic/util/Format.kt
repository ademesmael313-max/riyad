package com.riyadsalihin.amharic.util

import java.text.NumberFormat
import java.util.Locale

fun formatCount(value: Int): String =
    NumberFormat.getIntegerInstance(Locale.ENGLISH).format(value)
