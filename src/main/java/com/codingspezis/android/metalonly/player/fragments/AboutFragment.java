package com.codingspezis.android.metalonly.player.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.codingspezis.android.metalonly.player.BuildConfig;
import com.codingspezis.android.metalonly.player.LicenseActivity;
import com.codingspezis.android.metalonly.player.R;
import com.codingspezis.android.metalonly.player.StreamControlActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FromHtml;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EFragment(R.layout.fragment_about)
public class AboutFragment extends Fragment {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass().getSimpleName());
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
            StreamControlActivity.toastMessage(getActivity(), getString(R.string.no_mail_app));
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
        textAppVersion.setText(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
    }

    @Click({R.id.textSherlockLicenseApache, R.id.textAndroidannotationsLicenseApache,
            R.id.textSpringLicenseApache, R.id.textJacksonLicenseApache})
    void displayApacheLicense() {
        displayLicense(LicenseActivity.KEY_BU_LICENSE_APACHE);
    }

    @Click({R.id.textLazyListLicenseMIT})
    void displayMitLicense() {
        displayLicense(LicenseActivity.KEY_BU_LICENSE_MIT);
    }

    /**
     * displays specified license
     *
     * @param license license to display
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
