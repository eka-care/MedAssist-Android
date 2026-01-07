package com.eka.medassist.ui.chat.utility

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun Long.toLocalCalendar(): Calendar {
    return Calendar.getInstance().apply {
        timeInMillis = this@toLocalCalendar
    }
}

internal fun Long.toLocalDateKey(): String {
    val calendar = this.toLocalCalendar()
    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
}

internal fun Long.toFormattedTime(): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(this))
}

internal fun Long.isToday(): Boolean {
    val calendar = this.toLocalCalendar()
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

fun Long.isYesterday(): Boolean {
    val calendar = this.toLocalCalendar()
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
}

fun Long.getDateHeader(): String {
    return when {
        isToday() -> "Today"
        isYesterday() -> "Yesterday"
        else -> toLocalDateKey()
    }
}