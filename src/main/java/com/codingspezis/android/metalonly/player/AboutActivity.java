package com.codingspezis.android.metalonly.player;

import android.annotation.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v4.app.*;
import android.widget.*;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.*;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.res.*;

/**
 * 
 * this activity shows information about the application e.g. codingspezis.com
 * and used software
 * 
 * @author codingspezis.com
 * 
 */
@EActivity(R.layout.activity_about)
@SuppressLint("Registered")
public class AboutActivity extends SherlockActivity {

	@StringRes
	String mailaddress_codingspezis, app_name;

	@ViewById
	@FromHtml(R.string.aboutThisApp)
	TextView textAboutApp;

	@ViewById
	@FromHtml(R.string.url_sherlock)
	TextView textSherlockLink;

	@ViewById
	@FromHtml(R.string.url_lazylist)
	TextView textLazyListLink;

	@ViewById
	@FromHtml(R.string.url_androidannotations)
	TextView textAndroidannotationsLink;

    @ViewById
    TextView textAppVersion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	/**
	 * sends system intent ACTION_SEND (send mail)
	 * 
	 * @param strTo
	 *            receiver of mail
	 * @param strSubject
	 *            subject of mail
	 * @param strText
	 *            text of mail
	 */
	private void sendEmail(String strTo, String strSubject, String strText) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { strTo });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, strSubject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, strText);
		try {
			startActivity(Intent.createChooser(emailIntent, strTo));
		} catch (android.content.ActivityNotFoundException ex) {
			MainActivity.toastMessage(this, getString(R.string.no_mail_app));
		}
	}

	@Click(R.id.buttonFeedback)
	void buttonFeedbackClicked() {
		String subject = "[" + app_name + " " + AppVersion(this)
				+ "] Feedback, Fehler";
		sendEmail(mailaddress_codingspezis, subject, "");
	}

	@Click({R.id.textSherlockLicenseApache, R.id.textAndroidannotationsLicenseApache,
            R.id.textSpringLicenseApache, R.id.textJacksonLicenseApache})
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
		Intent licenseIntent = new Intent(getApplicationContext(), LicenseActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString(LicenseActivity.KEY_BU_LICENSE_NAME, license);
		licenseIntent.putExtras(bundle);
		startActivity(licenseIntent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Intent intent = new Intent(this, MainActivity.class);
			NavUtils.navigateUpTo(this, intent);
			return true;
		}
		return false;
	}

    /**
     * updates the app version label to the version string generated by gradle
     */
    @AfterViews
    public void setAppVersionLabel() {
        textAppVersion.setText(AppVersion(this));
    }

    /**
     * getter for the app version string
     */
    public static String AppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch(Exception e) {
            return "";
        }
    }
}
