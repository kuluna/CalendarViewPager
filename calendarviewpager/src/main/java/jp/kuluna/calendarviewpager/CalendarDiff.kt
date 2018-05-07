package jp.kuluna.calendarviewpager

import android.support.v7.util.DiffUtil

class CalendarDiff(private val old: List<Day>, private val new: List<Day>) : DiffUtil.Callback() {
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = old[oldItemPosition] == new[newItemPosition]

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = old[oldItemPosition].calendar.time == new[newItemPosition].calendar.time

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    fun calculateDiff(): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(this, false)
    }
}
