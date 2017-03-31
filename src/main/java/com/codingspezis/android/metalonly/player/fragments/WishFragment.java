package com.codingspezis.android.metalonly.player.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codingspezis.android.metalonly.player.BuildConfig;
import com.codingspezis.android.metalonly.player.R;
import com.codingspezis.android.metalonly.player.WishActivity;
import com.codingspezis.android.metalonly.player.utils.UrlConstants;
import com.codingspezis.android.metalonly.player.utils.jsonapi.MetalOnlyAPIWrapper;
import com.codingspezis.android.metalonly.player.utils.jsonapi.NoInternetException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@EFragment(R.layout.fragment_wish)
@OptionsMenu(R.menu.help)
public class WishFragment extends Fragment {
    private static final Logger LOGGER = LoggerFactory.getLogger(WishFragment.class.getSimpleName());
    // intent keys
    private static final String KEY_WISHES_ALLOWED = WishActivity.KEY_WISHES_ALLOWED;
    private static final String KEY_REGARDS_ALLOWED = WishActivity.KEY_REGARDS_ALLOWED;
    private static final String KEY_NUMBER_OF_WISHES = WishActivity.KEY_NUMBER_OF_WISHES;
    private static final String KEY_DEFAULT_INTERPRET = WishActivity.KEY_DEFAULT_INTERPRET;
    private static final String KEY_DEFAULT_TITLE = WishActivity.KEY_DEFAULT_TITLE;
    // shared preferences keys
    private static final String KEY_SP_NICK = "moa_nickname";
    @ViewById(R.id.btnSend)
    Button buttonSend;
    @ViewById
    EditText editNick,
            editArtist,
            editTitle,
            editRegard;
    @ViewById
    TextView textArtist,
            textTitle,
            textRegard;
    @ViewById(R.id.txtWishcount)
    TextView wishCount;
    private boolean wish, regard;
    private int numberOfWishes;
    @Bean
    MetalOnlyAPIWrapper apiWrapper;

    public WishFragment() {
    }

    private static void setInvisible(View v) {
        if (BuildConfig.DEBUG) LOGGER.debug("setInvisible({})", v);

        if (null != v) {
            v.setVisibility(View.GONE);
        }

        if (BuildConfig.DEBUG) LOGGER.debug("setInvisible({}) done", v);
    }

    @SuppressWarnings({"LocalVariableOfConcreteClass", "MethodReturnOfConcreteClass"})
    public static WishFragment newInstance(Bundle bundle) {
        if (BuildConfig.DEBUG) LOGGER.debug("newInstance({})", bundle);

        WishFragment wishFragment = new WishFragment_();
        wishFragment.setArguments(bundle);

        if (BuildConfig.DEBUG) LOGGER.debug("newInstance({}) done", bundle);
        return wishFragment;
    }

    @AfterViews
    void init() {
        if (BuildConfig.DEBUG) LOGGER.debug("init()");

        final String app_name = getString(R.string.app_name);
        final SharedPreferences settings = getActivity().getSharedPreferences(app_name,
                Context.MODE_MULTI_PROCESS);

        // input fields
        final String nickName = settings.getString(KEY_SP_NICK, "");
        editNick.setText(nickName);


        // get parameters
        Bundle bundle = getArguments();


        if (null != bundle) {
            wish = bundle.getBoolean(KEY_WISHES_ALLOWED, false);
            regard = bundle.getBoolean(KEY_REGARDS_ALLOWED, false);
            numberOfWishes = bundle.getInt(KEY_NUMBER_OF_WISHES);
            editArtist.setText(bundle.getString(KEY_DEFAULT_INTERPRET));
            editTitle.setText(bundle.getString(KEY_DEFAULT_TITLE));
        }

        wishCount.setText(String.valueOf(numberOfWishes));
        if (!wish) {
            editArtist.setText(R.string.no_wishes_short);
            editArtist.setEnabled(false);
            setInvisible(editArtist);
            setInvisible(textArtist);
            editTitle.setText(R.string.no_wishes_short);
            editTitle.setEnabled(false);
            setInvisible(editTitle);
            setInvisible(textTitle);

            wishCount.setText(wishCount.getText() + "\n" + getString(R.string.no_wishes_short));

        }
        if (!regard) {
            editRegard.setText(R.string.no_regards);
            editRegard.setEnabled(false);
            setInvisible(editRegard);
            setInvisible(textRegard);

            wishCount.setText(wishCount.getText() + "\n" + getString(R.string.no_regards));
        }

        if (BuildConfig.DEBUG) LOGGER.debug("init() done");

    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) LOGGER.debug("onPause()");

        final String app_name = getString(R.string.app_name);
        SharedPreferences settings = getActivity().getSharedPreferences(app_name,
                Context.MODE_MULTI_PROCESS);

        SharedPreferences.Editor editor = settings.edit();
        final String nickName = editNick.getText().toString();
        editor.putString(KEY_SP_NICK, nickName);
        editor.apply();
        super.onPause();

        if (BuildConfig.DEBUG) LOGGER.debug("onPause() done");
    }

    /**
     * checks edit text objects for valid data
     *
     * @return true if input is valid - false otherwise
     */
    private boolean haveValidData() {
        if (BuildConfig.DEBUG) LOGGER.debug("haveValidData()");

        boolean haveNick = !TextUtils.isEmpty(editNick.getText());
        final boolean artistEnabledAndHasText = !TextUtils.isEmpty(editArtist.getText()) && editArtist.isEnabled();
        final boolean titleEnabledAndHasText = !TextUtils.isEmpty(editTitle.getText())
                && editTitle.isEnabled();
        boolean haveWish = artistEnabledAndHasText
                && titleEnabledAndHasText;
        boolean haveRegard = !TextUtils.isEmpty(editRegard.getText())
                && editRegard.isEnabled();

        boolean isValidData = haveNick && (haveWish || haveRegard);

        if (BuildConfig.DEBUG) LOGGER.debug("haveValidData() -> {}", isValidData);
        return isValidData;
    }

    @Click(R.id.btnSend)
    void sendButtonClicked() {
        if (BuildConfig.DEBUG) LOGGER.debug("sendButtonClicked()");

        if (haveValidData()) {
            // TODO send data
            sendWishGreet();
        } else {
            notifyUser(R.string.invalid_input);
        }


        if (BuildConfig.DEBUG) LOGGER.debug("sendButtonClicked() done");
    }

    @Background
    void sendWishGreet() {
        try{
            final String nick = editNick.getText().toString();
            final String artist = editArtist.getText().toString();
            final String song = editTitle.getText().toString();
            final String greet = editRegard.getText().toString();

            String x;
            if(wish && !TextUtils.isEmpty(artist) && !TextUtils.isEmpty(song)) {
                x = apiWrapper.postWishAndGreetings(nick, artist, song, greet);
            }else{
                x = apiWrapper.postGreetings(nick, greet);
            }
               LOGGER.error(x);
        }catch (NoInternetException e){
            notifyUser(R.string.no_internet);
        }
    }

    @UiThread
    void notifyUser(String s) {
        Toast.makeText(getActivity(),
                s, Toast.LENGTH_SHORT)
                .show();
    }

    @UiThread
    void notifyUser(int id) {
        Toast.makeText(getActivity(),
                id, Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * shows info
     */
    @OptionsItem(R.id.mnu_help)
    void showInfo() {
        if (BuildConfig.DEBUG) LOGGER.debug("showInfo()");

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(R.string.notification);
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_text, null);
        TextView tv = (TextView) v.findViewById(R.id.text);
        tv.setText(R.string.wish_explanation);
        alert.setView(v);
        alert.setPositiveButton(R.string.ok, null);
        alert.show();


        if (BuildConfig.DEBUG) LOGGER.debug("showInfo() done");
    }

}
