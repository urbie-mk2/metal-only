package com.codingspezis.android.metalonly.player.plan

import org.androidannotations.annotations.EBean
import org.androidannotations.annotations.res.StringArrayRes
import org.androidannotations.annotations.res.StringRes

import java.util.ArrayList
import java.util.Calendar
import java.util.GregorianCalendar

@EBean
open class PlanEntryToItemConverter {

    @JvmField
    @StringRes
    protected var plan: String? = null

    @JvmField
    @StringArrayRes
    protected var days: Array<String>? = null

    internal var todayStartIndex: Int = 0

    fun convertToPlan(listEvents: ArrayList<PlanEntryAndDataUnification>): ArrayList<PlanItem> {
        // TODO refactor this method
        val listItems = ArrayList<PlanItem>()
        val cal = GregorianCalendar()

        var day = 0

        var nextDaySection = PlanSectionItem(days!![day])

        listItems.add(nextDaySection)

        for (i in listEvents.indices) {
            val d = listEvents[i]
            listItems.add(PlanRealEntryItem(d))
            if (hasNextListItem(listEvents, i)) {
                val nextItem = listEvents[i + 1]
                if (notOnSameDay(d, nextItem)) {
                    day++
                    nextDaySection = PlanSectionItem(days!![day])
                    listItems.add(nextDaySection)
                    if (isToday(day)) {
                        todayStartIndex = listItems.size - 1
                    }
                }
            }
        }
        return listItems
    }

    private fun isToday(dayIndex: Int): Boolean {
        val cal = GregorianCalendar()
        return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 == dayIndex
    }

    private fun notOnSameDay(d: PlanEntryAndDataUnification, nextItem: PlanEntryAndDataUnification): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = d.start()
        val thisDay = cal1.get(Calendar.DAY_OF_WEEK)

        val cal2 = Calendar.getInstance()
        cal2.time = nextItem.start()
        val nextItemDay = cal2.get(Calendar.DAY_OF_WEEK)

        return thisDay != nextItemDay
    }

    private fun hasNextListItem(listEvents: ArrayList<PlanEntryAndDataUnification>, i: Int): Boolean {
        return i < listEvents.size - 1
    }

    fun todayStartIndex(): Int {
        return todayStartIndex
    }
}