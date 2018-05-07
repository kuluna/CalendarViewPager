package jp.kuluna.calendarviewpager.sample

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import jp.kuluna.calendarviewpager.CalendarPagerAdapter
import jp.kuluna.calendarviewpager.CalendarViewPager
import jp.kuluna.calendarviewpager.Day
import jp.kuluna.calendarviewpager.DayState
import java.util.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setDateHeader(Calendar.getInstance())

        // setup adapter
        val viewPager = findViewById<CalendarViewPager>(R.id.calendar_view_pager)
        viewPager.adapter = CustomCalendarAdapter(this)

        // listeners
        viewPager.onDayClickListener = {
            Toast.makeText(this, it.calendar.time.toString(), Toast.LENGTH_SHORT).show()
        }
        viewPager.onDayLongClickListener = {
            Toast.makeText(this, "Long Clicked :" + it.calendar.time.toString(), Toast.LENGTH_SHORT).show()
            true
        }
        viewPager.onCalendarChangeListener = {
            setDateHeader(it)
        }
    }

    private fun setDateHeader(calendar: Calendar) {
        findViewById<TextView>(R.id.text_month).text = DateUtils.formatDateTime(this, calendar.timeInMillis,
                DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_MONTH_DAY)
    }
}

class CustomCalendarAdapter(context: Context) : CalendarPagerAdapter(context) {
    override fun onCreateView(parent: ViewGroup, viewType: Int): View {
        return LayoutInflater.from(context).inflate(R.layout.view_calendar_cell, parent, false)
    }

    override fun onBindView(view: View, day: Day) {
        if (day.state == DayState.ThisMonth) {
            view.visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.text_day).text = day.calendar.get(Calendar.DAY_OF_MONTH).toString()
            view.findViewById<View>(R.id.view_dot).visibility = if (day.isSelected) View.VISIBLE else View.GONE
        } else {
            view.visibility = View.INVISIBLE
        }
    }
}
