package com.codingspezis.android.metalonly.player.plan

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import com.codingspezis.android.metalonly.player.R
import com.codingspezis.android.metalonly.player.core.ShowInformation
import com.codingspezis.android.metalonly.player.helper.ShowInformationDateHelper

/**
 * ClickListener for entries in the plan shown by [PlanActivity]
 */
class PlanEntryClickListener(private val data: ShowInformation, private val context: Context) : DialogInterface.OnClickListener {

    val share: String by lazy { context.resources.getStringArray(R.array.plan_options_array)[1] }

    override fun onClick(dialog: DialogInterface, action: Int) {
        when (action) {
            ADD_TO_CALENDAR_ACTION -> addEntryToCalendar()
            SHARE_ACTION -> shareEntry()
        }
    }

    private fun addEntryToCalendar() {
        val description = data.moderator + "\n" + data.genre

        val intent = Intent(Intent.ACTION_EDIT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra("title", data.showTitle)
        intent.putExtra("description", description)
        intent.putExtra("beginTime", data.startDate.time)
        intent.putExtra("endTime", data.endDate.time)

        context.startActivity(intent)
    }

    private fun shareEntry() {
        val message = """|${formattedDateString()} ${startTimeString()} - ${endTimeString()}
                         |${data.showTitle}
                         |${data.moderator}
                         |${data.genre}""".trimMargin()

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        context.startActivity(Intent.createChooser(intent, share))
    }

    fun formattedDateString(): String {
        return ShowInformationDateHelper.formattedDateString(data)
    }

    fun startTimeString(): CharSequence {
        return ShowInformationDateHelper.startTimeString(data)
    }

    fun endTimeString(): String {
        return ShowInformationDateHelper.endTimeString(data)
    }

    companion object {
        private val ADD_TO_CALENDAR_ACTION = 0
        private val SHARE_ACTION = 1
    }
}