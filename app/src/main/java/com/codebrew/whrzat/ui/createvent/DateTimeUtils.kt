package com.codebrew.whrzat.ui.createvent


import java.util.Calendar
import java.util.Date

object DateTimeUtils {

    fun createDate(year: Int, monthOfYear: Int, dayOfMonth: Int): Date {
        val calendar = Calendar.getInstance()
        // Set the calendar to start of today calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        // Setup the date calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return calendar.time
    }
}

