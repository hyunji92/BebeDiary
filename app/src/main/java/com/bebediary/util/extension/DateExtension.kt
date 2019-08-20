package com.bebediary.util.extension

import java.text.SimpleDateFormat
import java.util.*

fun Date.format(dateFormat: String, locale: Locale = Locale.getDefault()): String {
    val simpleDateFormat = SimpleDateFormat(dateFormat, locale)
    return simpleDateFormat.format(this)
}

fun Date.isSaturday(): Boolean {
    return Calendar.getInstance().also { it.time = this }
        .get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
}

fun Date.isSunday(): Boolean {
    return Calendar.getInstance().also { it.time = this }
        .get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
}