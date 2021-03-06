package com.codingspezis.android.metalonly.player.plan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.support.v4.app.Fragment
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.codingspezis.android.metalonly.player.R
import com.github.ironjan.metalonly.client_library.MetalOnlyClient
import com.github.ironjan.metalonly.client_library.NoInternetException
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Background
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.EFragment
import org.androidannotations.annotations.FragmentArg
import org.androidannotations.annotations.ItemClick
import org.androidannotations.annotations.UiThread
import org.androidannotations.annotations.ViewById
import org.androidannotations.annotations.res.StringArrayRes
import org.androidannotations.annotations.res.StringRes
import org.springframework.web.client.RestClientException

@EFragment(R.layout.fragment_plan)
@SuppressLint("SimpleDateFormat", "Registered")
open class PlanFragment : Fragment() {

    @JvmField
    @FragmentArg
    var site: String? = null

    @JvmField
    @ViewById(android.R.id.list)
    var list: ListView? = null

    @JvmField
    @ViewById
    var loadingMessageTextView: TextView? = null
    @JvmField
    @ViewById(android.R.id.progress)
    var loadingProgressBar: ProgressBar? = null
    @JvmField
    @StringRes
    var plan_failed_to_load: String? = null
    @JvmField
    @ViewById(android.R.id.empty)
    var empty: View? = null

    @JvmField
    @Bean
    var planEntryToItemConverter: PlanEntryToItemConverter? = null

    @JvmField
    @StringRes
    var plan: String? = null
    @JvmField
    @StringArrayRes
    var days: Array<String>? = null
    @JvmField
    @StringRes
    var no_internet: String? = null

    @AfterViews
    internal fun bindEmptyViewToList() {
        list!!.emptyView = empty
    }

    @AfterViews
    @Background
    internal open fun loadPlan() {
        try {
            apiResponseReceived(MetalOnlyClient.getClient(activity).getPlan())
        } catch (e: NoInternetException) {
            updateEmptyViewOnFailure(no_internet)
        } catch (e: RestClientException) {
            // TODO Can we catch ResourceAccessException to HttpStatusCodeException show better info?
            val text = plan_failed_to_load + ":\n" + e.message
            updateEmptyViewOnFailure(text)
        }

    }

    @UiThread
    internal open fun apiResponseReceived(plan: com.github.ironjan.metalonly.client_library.Plan?) {
        if (plan == null) {
            updateEmptyViewOnFailure(plan_failed_to_load)
            return
        }
        if (activity == null) {
            return
        }

        val shows = plan.entries

        val listItems = planEntryToItemConverter!!.convertToPlan(shows)
        val adapter = PlanAdapter(activity, listItems)

        list!!.adapter = adapter
        list!!.setSelection(planEntryToItemConverter!!.todayStartIndex())
    }

    @UiThread
    internal open fun updateEmptyViewOnFailure(text: String?) {
        if (text != null) {
            loadingMessageTextView?.text = text
            loadingProgressBar?.visibility = View.GONE
        }
    }

    @ItemClick(android.R.id.list)
    internal fun entryClicked(item: Any) {
        if (item is PlanRealEntryItem) {
            val entryItem = item
            val builder = AlertDialog.Builder(activity)
            builder.setItems(R.array.plan_options_array, PlanEntryClickListener(entryItem.showInformation!!, activity))
            builder.show()
        }

    }
}
