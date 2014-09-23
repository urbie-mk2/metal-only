package com.codingspezis.android.metalonly.player.fragments;

import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.text.method.*;
import android.widget.*;

import com.actionbarsherlock.app.*;
import com.codingspezis.android.metalonly.player.*;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.*;
import org.slf4j.*;

@EFragment(R.layout.fragment_about)
public class AboutFragment extends SherlockFragment {
    @StringRes
    String mailaddress_codingspezis, app_name;

    @ViewById
    TextView textAppVersion;

    @ViewById(R.id.textAboutApp)
    @FromHtml(R.string.aboutThisApp)
    TextView mTextAboutApp;

    @ViewById
    @FromHtml(R.string.url_sherlock)
    TextView textSherlockLink;

    @ViewById
    @FromHtml(R.string.url_lazylist)
    TextView textLazyListLink;

    @ViewById
    @FromHtml(R.string.url_androidannotations)
    TextView textAndroidannotationsLink;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass().getSimpleName());

    /**
     * sends system intent ACTION_SEND (send mail)
     *
     * @param strTo      receiver of mail
     * @param strSubject subject of mail
     * @param strText    text of mail
     */
    private void sendEmail(String strTo, String strSubject, String strText) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{strTo});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, strSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, strText);
        try {
            startActivity(Intent.createChooser(emailIntent, strTo));
        } catch (android.content.ActivityNotFoundException ex) {
            MainActivity.toastMessage(getActivity(), getString(R.string.no_mail_app));
        }
    }

    @Click(R.id.buttonFeedback)
    void buttonFeedbackClicked() {
        String subject = "[" + app_name + " " + BuildConfig.VERSION_NAME
                + "] Feedback, Fehler";
        sendEmail(mailaddress_codingspezis, subject, "");
    }

    /**
     * updates the app version label to the version string generated by gradle
     */
    @AfterViews
    public void setAppVersionLabel() {
        textAppVersion.setText(BuildConfig.VERSION_NAME);
    }

    @Click({ R.id.textSherlockLicenseApache, R.id.textAndroidannotationsLicenseApache,
             R.id.textSpringLicenseApache,   R.id.textJacksonLicenseApache })
    void displayApacheLicense() {
        displayLicense(LicenseActivity.KEY_BU_LICENSE_APACHE);
    }

    @Click({ R.id.textLazyListLicenseMIT })
    void displayMitLicense() {
        displayLicense(LicenseActivity.KEY_BU_LICENSE_MIT);
    }

    /**
     * displays specified license
     *
     * @param license
     *            license to display
     */
    private void displayLicense(String license) {
        Intent licenseIntent = new Intent(getActivity().getApplicationContext(),
                LicenseActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LicenseActivity.KEY_BU_LICENSE_NAME, license);
        licenseIntent.putExtras(bundle);
        startActivity(licenseIntent);
    }
}