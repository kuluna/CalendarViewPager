package jp.kuluna.calendarviewpager

import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.util.*

abstract class DayGridAdapter : BaseAdapter {
    private val context: Context
    private val calendar: Calendar
    private val weekOfMonth: Int
    private val startDate: Calendar

    lateinit var items: List<Day>

    constructor(context: Context, date: Date, preselectedDay: Date? = null) : this(context, Calendar.getInstance().apply { time = date }, preselectedDay)

    constructor(context: Context, calendar: Calendar, preselectedDay: Date? = null) : super() {
        this.context = context
        this.calendar = calendar
        this.weekOfMonth = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)

        // Viewのはじめの日を求める
        val start = org.apache.commons.lang.time.DateUtils.truncate(calendar, Calendar.DAY_OF_MONTH)
        start.set(Calendar.DAY_OF_MONTH, 1)
        start.add(Calendar.DAY_OF_MONTH, -start.get(Calendar.DAY_OF_WEEK) + 1)
        startDate = start

        updateItems(preselectedDay)
    }

    fun updateItems(selectedDate: Date? = null) {
        this.items = (0..count).map {
            val cal = Calendar.getInstance().apply { time = startDate.time }
            cal.add(Calendar.DAY_OF_MONTH, it)

            val state = when (calendar.get(Calendar.MONTH).compareTo(cal.get(Calendar.MONTH))) {
                -1 -> DayState.NextMonth
                0 -> DayState.ThisMonth
                1 -> DayState.PreviousMonth
                else -> throw IllegalStateException()
            }
            val isSelected = when (selectedDate) {
                null -> false
                else -> org.apache.commons.lang.time.DateUtils.isSameDay(cal.time, selectedDate)
            }
            val isToday = DateUtils.isToday(cal.timeInMillis)

            Day(cal, state, isToday, isSelected)
        }
    }

    override fun getView(position: Int, containerView: View?, parent: ViewGroup?): View = getView(items[position], containerView, parent)

    override fun getItem(position: Int): Day = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = 7 * weekOfMonth

    abstract fun getView(day: Day, containerView: View?, parent: ViewGroup?): View
}

data class Day(
        var calendar: Calendar = Calendar.getInstance(),
        var state: DayState = DayState.PreviousMonth,
        var isToday: Boolean = false,
        var isSelected: Boolean = false
)

enum class DayState {
    PreviousMonth,
    ThisMonth,
    NextMonth
}
