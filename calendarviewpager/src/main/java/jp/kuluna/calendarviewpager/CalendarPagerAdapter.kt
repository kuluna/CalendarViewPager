package jp.kuluna.calendarviewpager

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.apache.commons.lang3.time.DateUtils
import java.util.*

/**
 * @param isStartAtMonday カレンダー表記を月曜開始にするかどうか。falseにすると日曜開始になります
 */
open class CalendarPagerAdapter(val context: Context, base: Calendar = Calendar.getInstance(), val startingAt: DayOfWeek = DayOfWeek.Sunday) : PagerAdapter() {
    private val baseCalendar: Calendar = DateUtils.truncate(base, Calendar.DAY_OF_MONTH).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        // 週が何曜日からはじまるか
        // 初期値はJP/USなどでは「1(SUNDAY)」だが、一部地域(UK)は「2(MONDAY)」なので引数で指定された曜日へ変更しておく
        firstDayOfWeek = Calendar.SUNDAY + startingAt.getDifference()
        // 最初の週を何日以上とするか
        // JP/USなどは「1」だが、一部地域(UK)の場合は「4」となるため対象月の週数の算出結果が想定より少ない値となってしまう
        minimalDaysInFirstWeek = 1
    }
    private var viewContainer: ViewGroup? = null

    /** 選択されている日 */
    var selectedDay: Date? = null
        set(value) {
            field = value
            notifyCalendarItemChanged()
        }
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
        val recyclerView = RecyclerView(context).apply {
            layoutManager = GridLayoutManager(context, 7) // 1週間 なので
            isNestedScrollingEnabled = false
            hasFixedSize()

            adapter = object : CalendarCellAdapter(context, getCalendar(position), startingAt, selectedDay) {
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, day: Day) {
                    holder.itemView.setOnClickListener {
                        this@CalendarPagerAdapter.selectedDay = day.calendar.time
                        this@CalendarPagerAdapter.onDayClickLister?.invoke(day)
                        notifyCalendarItemChanged()
                    }
                    holder.itemView.setOnLongClickListener {
                        if (this@CalendarPagerAdapter.onDayLongClickListener != null) {
                            this@CalendarPagerAdapter.onDayLongClickListener!!.invoke(day)
                        } else {
                            false
                        }
                    }
                    this@CalendarPagerAdapter.onBindView(holder.itemView, day)
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                        object : RecyclerView.ViewHolder(this@CalendarPagerAdapter.onCreateView(parent, viewType)) {}

            }
        }
        container.addView(recyclerView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        viewContainer = container

        return recyclerView
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
    fun notifyCalendarChanged() {
        val views = viewContainer ?: return
        (0 until views.childCount).forEach { i ->
            ((views.getChildAt(i) as? RecyclerView)?.adapter as? CalendarCellAdapter)?.run {
                notifyItemRangeChanged(0, items.size)
            }
        }
    }

    private fun notifyCalendarItemChanged() {
        val views = viewContainer ?: return
        (0 until views.childCount).forEach { i ->
            ((views.getChildAt(i) as? RecyclerView)?.adapter as? CalendarCellAdapter)?.updateItems(selectedDay)
        }
    }

    open fun onCreateView(parent: ViewGroup, viewType: Int): View {
        return TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 96)
        }
    }

    open fun onBindView(view: View, day: Day) {
        val textView = view as TextView
        textView.text = when (day.state) {
            DayState.ThisMonth -> day.calendar.get(Calendar.DAY_OF_MONTH).toString()
            else -> ""
        }
    }

    enum class DayOfWeek {
        Sunday,
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday;

        /** 基準(日曜日)との日数の差 */
        fun getDifference(): Int {
            return when (this) {
                Sunday -> 0
                Monday -> 1
                Tuesday -> 2
                Wednesday -> 3
                Thursday -> 4
                Friday -> 5
                Saturday -> 6
            }
        }

        /** 最初の週が少なくなっている */
        fun isLessFirstWeek(calendar: Calendar): Boolean {
            return calendar.get(Calendar.DAY_OF_WEEK) < getDifference() + 1
        }

        /** 最後の週が多くなっている */
        fun isMoreLastWeek(calendar: Calendar): Boolean {
            val end = DateUtils.truncate(calendar, Calendar.DAY_OF_MONTH)
            end.add(Calendar.MONTH, 1)
            end.add(Calendar.DATE, -1)
            return end.get(Calendar.DAY_OF_WEEK) < getDifference() + 1
        }
    }
}
