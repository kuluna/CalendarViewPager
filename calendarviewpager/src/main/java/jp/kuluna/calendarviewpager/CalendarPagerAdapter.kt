package jp.kuluna.calendarviewpager

import android.content.Context
import android.os.Handler
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import org.apache.commons.lang.time.DateUtils
import java.util.*

open class CalendarPagerAdapter(val context: Context, base: Calendar = Calendar.getInstance()) : PagerAdapter() {
    private val baseCalendar: Calendar = DateUtils.truncate(base, Calendar.DAY_OF_MONTH).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    private var viewContainer: ViewGroup? = null

    /** 選択されている日 */
    var selectedDay: Date? = null
    /** 日をクリックした時のコールバックイベントを実行します */
    var onDayClickLister: ((Day) -> Unit)? = null
    /** 日をロングクリックした時のコールバックイベントを実行します */
    var onDayLongClickListener: ((Day) -> Boolean)? = null

    companion object {
        /** 最大ページ */
        const val MAX_VALUE = 500
    }

    override fun getCount(): Int = MAX_VALUE

    /** カレンダーViewを生成します */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val gridView = GridView(context).apply {
            val gridAdapter = object : DayGridAdapter(context!!, getCalendar(position), selectedDay) {
                override fun getView(day: Day, containerView: View?, parent: ViewGroup?): View = this@CalendarPagerAdapter.getView(day, containerView, parent)
            }

            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            numColumns = 7 // 1週間 なので
            adapter = gridAdapter
            setOnItemClickListener { _, _, position, _ ->
                val day = gridAdapter.items[position]
                onDayClickLister?.invoke(day)

                // クリック後の再描画イベントを走らせる
                selectedDay = day.calendar.time
                notifyDataSetChangedInContainerView()
            }
            setOnItemLongClickListener { _, _, position, _ ->
                onDayLongClickListener?.invoke(gridAdapter.items[position]) ?: true
            }
        }
        container.addView(gridView)
        viewContainer = container

        return gridView
    }

    /** 画面外のカレンダーViewを消去します */
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = (view == `object`)

    fun getCalendar(position: Int): Calendar {
        return (baseCalendar.clone() as Calendar).apply {
            add(Calendar.MONTH, position - MAX_VALUE / 2)
        }
    }

    /** PagerAdapter内にあるカレンダーを再描画します */
    fun notifyDataSetChangedInContainerView() {
        val views = viewContainer ?: return
        (0 until views.childCount).forEach { i ->
            ((views.getChildAt(i) as GridView).adapter as DayGridAdapter).run {
                updateItems(selectedDay)
                Handler().post { notifyDataSetChanged() }
            }
        }
    }

    open fun getView(day: Day, containerView: View?, parent: ViewGroup?): View {
        return TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 96)
            text = when (day.state) {
                DayState.ThisMonth -> day.calendar.get(Calendar.DAY_OF_MONTH).toString()
                else -> ""
            }
        }
    }
}
