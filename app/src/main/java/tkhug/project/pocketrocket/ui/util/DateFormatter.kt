package tkhug.project.pocketrocket.ui.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {

    private val locale = Locale.getDefault()

    fun formatPeriodRange(startMillis: Long, endMillis: Long): String {
        val fmt = SimpleDateFormat("MM.dd", locale)
        return "${fmt.format(Date(startMillis))}~${fmt.format(Date(endMillis))}"
    }

    fun formatMonthYear(millis: Long): String {
        val fmt = SimpleDateFormat("MMMM yyyy", locale)
        return fmt.format(Date(millis))
    }

    fun formatDate(millis: Long): String {
        val fmt = SimpleDateFormat("dd MMM yyyy", locale)
        return fmt.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val fmt = SimpleDateFormat("dd MMM", locale)
        return fmt.format(Date(millis))
    }

    fun formatDayOfWeek(millis: Long): String {
        val fmt = SimpleDateFormat("EEE", locale)
        return fmt.format(Date(millis))
    }

    /** Returns epoch millis for the first millisecond of the current month */
    fun currentMonthStart(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /** Returns epoch millis for the last millisecond of the current month */
    fun currentMonthEnd(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /** Shift start/end by [months] months */
    fun shiftMonth(startMillis: Long, endMillis: Long, months: Int): Pair<Long, Long> {
        val newStart = Calendar.getInstance().apply {
            timeInMillis = startMillis
            add(Calendar.MONTH, months)
        }.timeInMillis
        val newEnd = Calendar.getInstance().apply {
            timeInMillis = newStart
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }.timeInMillis
        return newStart to newEnd
    }

    /**
     * Finance-month helpers: allow months that start on an arbitrary day of month.
     * startDay: 1..31. If the month does not contain [startDay], the day is capped to the month's max day.
     */
    fun financeMonthStartForTimestamp(tsMillis: Long, startDay: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = tsMillis

        val cand = Calendar.getInstance()
        cand.timeInMillis = tsMillis
        val max = cand.getActualMaximum(Calendar.DAY_OF_MONTH)
        cand.set(Calendar.DAY_OF_MONTH, if (startDay > max) max else startDay)
        cand.set(Calendar.HOUR_OF_DAY, 0); cand.set(Calendar.MINUTE, 0); cand.set(Calendar.SECOND, 0); cand.set(Calendar.MILLISECOND, 0)

        if (tsMillis < cand.timeInMillis) {
            // timestamp is before this month's candidate start -> use previous month
            cand.add(Calendar.MONTH, -1)
            val max2 = cand.getActualMaximum(Calendar.DAY_OF_MONTH)
            cand.set(Calendar.DAY_OF_MONTH, if (startDay > max2) max2 else startDay)
            cand.set(Calendar.HOUR_OF_DAY, 0); cand.set(Calendar.MINUTE, 0); cand.set(Calendar.SECOND, 0); cand.set(Calendar.MILLISECOND, 0)
        }
        return cand.timeInMillis
    }

    fun financeMonthEndForStart(startMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startMillis
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        return cal.timeInMillis
    }

    /** Compute end millis for a finance-month that begins at [startMillis] and ends on [endDay].
     * If endDay < startDay the end falls in the next month; otherwise it's in the same month.
     */
    fun financeMonthEndForStart(startMillis: Long, endDay: Int, startDay: Int): Long {
        val startCal = Calendar.getInstance().apply { timeInMillis = startMillis }
        val cal = Calendar.getInstance()
        // determine if end is in same month or next month
        if (endDay >= startDay) {
            cal.timeInMillis = startMillis
        } else {
            // end is in next month
            cal.timeInMillis = startMillis
            cal.add(Calendar.MONTH, 1)
        }
        val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, if (endDay > max) max else endDay)
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun financeMonthRangeForNow(startDay: Int): Pair<Long, Long> = financeMonthRangeForNow(startDay, startDay - 1)

    fun financeMonthRangeForNow(startDay: Int, endDay: Int): Pair<Long, Long> {
        val s = financeMonthStartForTimestamp(System.currentTimeMillis(), startDay)
        val e = financeMonthEndForStart(s, endDay, startDay)
        return s to e
    }

    fun shiftFinanceMonth(startMillis: Long, endMillis: Long, months: Int, startDay: Int): Pair<Long, Long> =
        shiftFinanceMonth(startMillis, endMillis, months, startDay, startDay - 1)

    fun shiftFinanceMonth(startMillis: Long, endMillis: Long, months: Int, startDay: Int, endDay: Int): Pair<Long, Long> {
        val newStartCal = Calendar.getInstance().apply {
            timeInMillis = startMillis
            add(Calendar.MONTH, months)
        }
        // ensure day-of-month remains valid (cap if necessary)
        val max = newStartCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dom = newStartCal.get(Calendar.DAY_OF_MONTH)
        if (dom > max) newStartCal.set(Calendar.DAY_OF_MONTH, max)
        newStartCal.set(Calendar.HOUR_OF_DAY, 0); newStartCal.set(Calendar.MINUTE, 0); newStartCal.set(Calendar.SECOND, 0); newStartCal.set(Calendar.MILLISECOND, 0)

        val newEnd = financeMonthEndForStart(newStartCal.timeInMillis, endDay, startDay)

        return newStartCal.timeInMillis to newEnd
    }
}

