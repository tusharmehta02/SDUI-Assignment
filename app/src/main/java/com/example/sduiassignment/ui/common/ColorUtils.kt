package com.example.sduiassignment.ui.common

import android.graphics.Color

fun parseHexColorOrNull(hex: String?): Int? =
    hex?.let { runCatching { Color.parseColor(it) }.getOrNull() }
