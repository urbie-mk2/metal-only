package com.codingspezis.android.metalonly.player.fragments

import android.support.v4.app.Fragment
import android.widget.CheckBox
import android.widget.TextView

import com.codingspezis.android.metalonly.player.BuildConfig
import com.codingspezis.android.metalonly.player.R
import com.codingspezis.android.metalonly.player.crashlytics.CrashlyticsPrefs_
import com.codingspezis.android.metalonly.player.utils.FeedbackMailer

import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.EFragment
import org.androidannotations.annotations.ViewById
import org.androidannotations.annotations.res.StringRes
import org.androidannotations.annotations.sharedpreferences.Pref

/**
 * Displays information about this app and links to github.
 */
@EFragment(R.layout.fragment_about)
open class AboutFragment : Fragment() {
    @JvmField
    @StringRes
    internal var mailaddress_codingspezis: String? = null

    @JvmField
    @StringRes
    internal var app_name: String? = null

    @JvmField
    @ViewById
    internal var textAppVersion: TextView? = null

    @JvmField
    @Bean
    internal var feedbackMailer: FeedbackMailer? = null

    @JvmField
    @ViewById
    internal var checkboxNoCrashReports: CheckBox? = null

    @JvmField
    @Pref
    internal var prefs: CrashlyticsPrefs_? = null

    @Click(R.id.buttonFeedback)
    internal fun buttonFeedbackClicked() {
        feedbackMailer!!.sendEmail()
    }

    /**
     * updates the app version label to the version string generated by gradle
     */
    @AfterViews
    fun setAppVersionLabel() {
        textAppVersion!!.text = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"
    }

    @AfterViews
    fun loadPrefs() {
        val isCrashlyticsDisabled = prefs!!.isCrashlyticsDisabled.get()
        checkboxNoCrashReports!!.isChecked = isCrashlyticsDisabled
    }

    override fun onPause() {
        val isCrashlyticsDisabled = checkboxNoCrashReports!!.isChecked
        prefs!!.edit().isCrashlyticsDisabled.put(isCrashlyticsDisabled).apply()
        super.onPause()
    }

}
