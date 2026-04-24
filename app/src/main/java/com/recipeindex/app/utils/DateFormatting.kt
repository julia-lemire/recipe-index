package com.recipeindex.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateFormatting {

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        return SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }

    /** "Nov 18–22", "Nov 28 – Dec 5", "Dec 28 – Jan 2", or partial-range strings. */
    fun formatDateRange(startDate: Long?, endDate: Long?): String {
        if (startDate == null && endDate == null) return ""
        if (startDate != null && endDate != null) {
            val start = Date(startDate)
            val end = Date(endDate)
            val cal = Calendar.getInstance()

            cal.time = start
            val startMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(start)
            val startDay = cal.get(Calendar.DAY_OF_MONTH)
            val startYear = cal.get(Calendar.YEAR)

            cal.time = end
            val endMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(end)
            val endDay = cal.get(Calendar.DAY_OF_MONTH)
            val endYear = cal.get(Calendar.YEAR)

            return if (startMonth == endMonth && startYear == endYear) {
                "$startMonth $startDay–$endDay"
            } else if (startYear == endYear) {
                "$startMonth $startDay – $endMonth $endDay"
            } else {
                "$startMonth $startDay – $endMonth $endDay"
            }
        }
        if (startDate != null) {
            return "From ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(startDate))}"
        }
        return "Until ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(endDate!!))}"
    }

    /** Generate a plan name from a date range, e.g. "Week of Nov 18" or "Nov 28 – Dec 5". */
    fun autoNameFromDateRange(startDate: Long?, endDate: Long?): String {
        if (startDate == null) return ""
        val cal = Calendar.getInstance().apply { timeInMillis = startDate }
        val endCal = if (endDate != null) Calendar.getInstance().apply { timeInMillis = endDate } else null

        // Exactly 7 days → "Week of Nov 18"
        if (endCal != null) {
            val diffDays = ((endDate!! - startDate) / (1000 * 60 * 60 * 24)).toInt()
            if (diffDays in 6..7) {
                return "Week of ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(startDate))}"
            }
        }
        return formatDateRange(startDate, endDate)
    }

    fun formatTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        return when {
            months > 0 -> "$months month${if (months == 1L) "" else "s"} ago"
            weeks > 0 -> "$weeks week${if (weeks == 1L) "" else "s"} ago"
            days > 0 -> "$days day${if (days == 1L) "" else "s"} ago"
            hours > 0 -> "$hours hour${if (hours == 1L) "" else "s"} ago"
            minutes > 0 -> "$minutes minute${if (minutes == 1L) "" else "s"} ago"
            else -> "just now"
        }
    }
}
