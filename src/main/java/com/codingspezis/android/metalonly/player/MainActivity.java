package com.codingspezis.android.metalonly.player;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.PorterDuff.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.view.Window;
import com.codingspezis.android.metalonly.player.favorites.*;
import com.codingspezis.android.metalonly.player.plan.*;
import com.codingspezis.android.metalonly.player.siteparser.*;
import com.codingspezis.android.metalonly.player.stream.*;
import com.codingspezis.android.metalonly.player.utils.jsonapi.*;
import com.codingspezis.android.metalonly.player.views.*;
import com.codingspezis.android.metalonly.player.wish.*;

import org.slf4j.*;

import java.net.*;
import java.text.*;
import java.util.*;

/**
 * main GUI activity
 * TODO use androidannotatons
 * TODO move more functionality out of this class
 */
public class MainActivity extends SherlockListActivity implements
        OnClickListener, OnItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final Logger LOGGER = LoggerFactory.getLogger(TAG);

    // intent keys
    public static final String showToastMessage = "MO_SHOW_TOAST";
    // intent extra key
    public static final String messageExtra = "MO_MESSAGE_EXTRA";

    // shared preferences keys
    public static final String KEY_SP_MODTHUMBDATE = "MO_SP_MODTHUMBDATE_";

    private MetalOnlyAPIWrapper apiWrapper;

    // GUI objects
    private final MainActivity mainActivity = this;
    private ListView listView;
    private ImageView buttonStream;
    private ImageButton buttonCalendar;
    private ImageButton buttonWish;
    private Marquee marqueeMod;
    private Marquee marqueeGenre;
    private Menu menu;

    // other
    private MainBroadcastReceiver broadcastReceiver;
    private MetadataParser metadataParser;
    private SongSaver favoritesSaver;
    private SongSaver historySaver;

    // other variables
    private boolean shouldPlay = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) LOGGER.debug("onCreate({})", savedInstanceState);

        setTheme(R.style.Theme_Sherlock);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setHomeButtonEnabled(false);
        setContentView(R.layout.activity_stream);
        setSupportProgressBarIndeterminateVisibility(false);
        setUpBroadcastReceiver();
        setUpPlayerService();
        setUpDataObjects();
        setUpGUIObjects();

        if (BuildConfig.DEBUG) LOGGER.debug("onCreate({}) done", savedInstanceState);
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) LOGGER.debug("onPause()");
        favoritesSaver.saveSongsToStorage();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (BuildConfig.DEBUG) LOGGER.debug("onResume()");

        super.onResume();
        favoritesSaver.reload();
        refreshShowInfo();
    }

    public void refreshShowInfo() {
        if (BuildConfig.DEBUG) LOGGER.debug("refreshShowInfo()");

        Runnable runnable = new Runnable() {

            private static final String TAG = "Runnable - refreshShowInfo()";
            private final Logger LOGGER = LoggerFactory.getLogger(TAG);

            @Override
            public void run() {
                if (BuildConfig.DEBUG) LOGGER.debug("run()");
                try {
                    Stats stats = apiWrapper.getStats();
                    String moderator = stats.getModerator();
                    String genre = stats.getGenre();
                    updateShowinfo(moderator, genre);
                } catch (NoInternetException e) {
                    // do nothing  if there is no internet connection
                }
            }

            private void updateShowinfo(final String moderator,
                                        final String genre) {
                if (BuildConfig.DEBUG) LOGGER.debug("updateShowinfo({},{})", moderator, genre);

                Runnable runnable = new Runnable() {

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        marqueeMod.setText(moderator);
                        marqueeGenre.setText(genre);
                        setWishButtonEnabled(!moderator.toLowerCase().startsWith("metalhead"));
                    }
                };
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(runnable);
            }
        };
        new Thread(runnable).start();

    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) LOGGER.debug("onDestroy()");
        Intent tmpIntent = new Intent(PlayerService.INTENT_EXIT);
        sendBroadcast(tmpIntent);
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * sets up data objects
     */
    private void setUpDataObjects() {
        if (BuildConfig.DEBUG) LOGGER.debug("setUpDataObjects()");
        apiWrapper = MetalOnlyAPIWrapper_.getInstance_(getApplicationContext());
        favoritesSaver = new SongSaver(this, FavoritesActivity.JSON_FILE_FAV,
                -1);
        setMetadataParser(new MetadataParser("-"));
    }

    /**
     * initializes main broadcast receiver with filters
     */
    private void setUpBroadcastReceiver() {
        if (BuildConfig.DEBUG) LOGGER.debug("setUpBroadcastReceiver()");
        broadcastReceiver = new MainBroadcastReceiver(this);
        registerReceiver(broadcastReceiver, new IntentFilter(
                PlayerService.INTENT_STATUS));
        registerReceiver(broadcastReceiver, new IntentFilter(
                PlayerService.INTENT_METADATA));
        registerReceiver(broadcastReceiver, new IntentFilter(showToastMessage));
    }

    /**
     * initializes player service and requests status
     */
    private void setUpPlayerService() {
        if (BuildConfig.DEBUG) LOGGER.debug("setUpPlayerService()");
        Intent playerStartIntent = new Intent(getApplicationContext(),
                PlayerService.class);
        startService(playerStartIntent);
        Intent statusIntent = new Intent(PlayerService.INTENT_STATUS_REQUEST);
        sendBroadcast(statusIntent);
    }

    /**
     * initializes GUI objects of main activity
     */
    private void setUpGUIObjects() {
        if (BuildConfig.DEBUG) LOGGER.debug("setUpGUIObjects()");
        buttonStream = (ImageView) findViewById(R.id.buttonPlay);
        buttonCalendar = (ImageButton) findViewById(R.id.btnCalendar);
        buttonWish = (ImageButton) findViewById(R.id.btnWish);
        marqueeMod = (Marquee) findViewById(R.id.marqueeMod);
        marqueeGenre = (Marquee) findViewById(R.id.marqueeGenree);
        listView = (ListView) findViewById(android.R.id.list);

        buttonStream.setOnClickListener(this);

        buttonCalendar.setOnClickListener(this);
        buttonWish.setOnClickListener(this);
        listView.setOnItemClickListener(this);

        toggleStreamButton(false);
        displaySongs();
        clearMetadata();
        if (BuildConfig.DEBUG) LOGGER.debug("setUpGUIObjects() done");
    }

    /**
     * sets the wish / regard button to en- or disabled
     */
    private void setWishButtonEnabled(boolean enabled) {
        if (BuildConfig.DEBUG) LOGGER.debug("setWishButtonEnabled({})", enabled);

        ImageButton btnWish = (ImageButton) findViewById(R.id.btnWish);
        setImageButtonEnabled(this, enabled, btnWish, R.drawable.mo_pen);
        if (BuildConfig.DEBUG) LOGGER.debug("setWishButtonEnabled({}) done", enabled);
    }

    /**
     * THIS METHOD WAS FOUND AT:
     * http://stackoverflow.com/questions/8196206/disable-an-imagebutton
     * <p/>
     * Sets the image button to the given state and grays-out the icon.
     *
     * @param enabled   The state of the button
     * @param item      The button item to modify
     * @param iconResId The button's icon ID
     */
    private void setImageButtonEnabled(Context ctxt, boolean enabled, ImageButton item, int iconResId) {
        if (BuildConfig.DEBUG)
            LOGGER.debug("setImageButtonEnabled({},{},{},{})", new Object[]{ctxt, enabled, item, iconResId});

        item.setEnabled(enabled);
        Drawable originalIcon = ctxt.getResources().getDrawable(iconResId);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);
        if (BuildConfig.DEBUG)
            LOGGER.debug("setImageButtonEnabled({},{},{},{}) done", new Object[]{ctxt, enabled, item, iconResId});
    }

    /**
     * THIS METHOD WAS FOUND AT:
     * http://stackoverflow.com/questions/8196206/disable-an-imagebutton
     * <p/>
     * Mutates and applies a filter that converts the given drawable to a Gray
     * image. This method may be used to simulate the color of disable icons in
     * Honeycomb's ActionBar.
     *
     * @return a mutated version of the given drawable with a color filter applied.
     */
    private Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (BuildConfig.DEBUG) LOGGER.debug("convertDrawableToGrayScale({})", drawable);

        if (drawable == null)
            return null;
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, Mode.SRC_IN);
        if (BuildConfig.DEBUG) LOGGER.debug("convertDrawableToGrayScale({}) done", drawable);
        return res;
    }

    /**
     * displays history songs on screen
     */
    public void displaySongs() {
        if (BuildConfig.DEBUG) LOGGER.debug("displaySongs()");

        historySaver = new SongSaver(this, PlayerService.JSON_FILE_HIST,
                PlayerService.HISTORY_ENTRIES);
        listView.removeAllViewsInLayout();
        ArrayList<Song> data = new ArrayList<Song>();

        for (int i = historySaver.size() - 1; i >= 0; i--) {
            final Song song = historySaver.get(i);
            data.add(song);
        }

        SongAdapter adapter = new SongAdapter(this, data);
        listView.setAdapter(adapter);
        if (BuildConfig.DEBUG) LOGGER.debug("displaySongs() done");
    }

    /**
     * toggles background color of stream button
     *
     * @param listening if this value is true the button shows stop from now on;
     *                  otherwise play is false
     */
    public void toggleStreamButton(boolean listening) {
        if (BuildConfig.DEBUG) LOGGER.debug("toggleStreamButton({})", listening);

        if (listening) {
            buttonStream.setImageResource(R.drawable.mo_stop5);
        } else {
            buttonStream.setImageResource(R.drawable.mo_play5);
        }
        if (BuildConfig.DEBUG) LOGGER.debug("toggleStreamButton({}) done", listening);
    }

    /**
     * generates options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) LOGGER.debug("onCreateOptionsMenu({})", menu);

        this.menu = menu;
        // favorites button
        MenuItem fav = menu.add(0, R.id.mnu_favorites, 0,
                R.string.menu_favorites);
        fav.setIcon(R.drawable.mo_star_b5);
        fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        // menu button
        SubMenu sub = menu.addSubMenu(0, R.id.mnu_sub, 0, R.string.menu);
        sub.setIcon(R.drawable.ic_core_unstyled_action_overflow);
        sub.add(0, R.id.mnu_donation, 0, R.string.menu_donation);
        sub.add(0, R.id.mnu_settings, 0, R.string.menu_settings);
        sub.add(0, R.id.mnu_info, 0, R.string.menu_info);
        sub.getItem().setShowAsAction(
                MenuItem.SHOW_AS_ACTION_ALWAYS
                        | MenuItem.SHOW_AS_ACTION_WITH_TEXT
        );
        if (BuildConfig.DEBUG) LOGGER.debug("onCreateOptionsMenu({}) done", menu);
        return true;
    }

    /**
     * handles menu button actions
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (BuildConfig.DEBUG) LOGGER.debug("onOptionsItemSelected({})", item);


        if (item.getItemId() == R.id.mnu_settings) {
            Intent settingsIntent = new Intent(getApplicationContext(),
                    SettingsActivity.class);
            startActivity(settingsIntent);
        } else if (item.getItemId() == R.id.mnu_favorites) {
            FavoritesActivity_.intent(this).start();
        } else if (item.getItemId() == R.id.mnu_donation) {
            Intent paypalIntent = new Intent(getApplicationContext(),
                    PayPalDonationActivity.class);
            startActivity(paypalIntent);
        } else if (item.getItemId() == R.id.mnu_info) {
            AboutActivity_.intent(this).start();
        } else {
            if (BuildConfig.DEBUG) LOGGER.debug("onOptionsItemSelected({}) done", item);
            return false;
        }
        if (BuildConfig.DEBUG) LOGGER.debug("onOptionsItemSelected({}) done", item);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG) LOGGER.debug("onKeyUp({},{})", keyCode, event);

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.getAction() == KeyEvent.ACTION_UP && menu != null
                    && menu.findItem(R.id.mnu_sub) != null) {
                menu.performIdentifierAction(R.id.mnu_sub, 0);
                return true;
            }
        }
        if (BuildConfig.DEBUG) LOGGER.debug("onKeyUp({},{}) done", keyCode, event);

        return super.onKeyUp(keyCode, event);
    }

    // button is not usable for MIN_BOTTON_DELAY msecs
    static long lastButtonToggle = 0;
    final static long MIN_BOTTON_DELAY = 1000;

    /**
     * handles button clicks *
     */
    @Override
    public void onClick(View arg0) {
        if (BuildConfig.DEBUG) LOGGER.debug("onClick({})", arg0);


        long currentTime = System.currentTimeMillis();
        // stream start / stop
        if (arg0 == buttonStream
                && currentTime - lastButtonToggle >= MIN_BOTTON_DELAY) {
            lastButtonToggle = System.currentTimeMillis();
            if (isShouldPlay()) {
                stopListening();
            } else {
                if (!HTTPGrabber.displayNetworkSettingsIfNeeded(this)) {
                    startListening();
                }
            }
        }
        // plan
        else if (arg0 == buttonCalendar) {
            if (!HTTPGrabber.displayNetworkSettingsIfNeeded(this)) {
                startPlanActivity();
            }
        }
        // wish
        else if (arg0 == buttonWish) {
            if (!HTTPGrabber.displayNetworkSettingsIfNeeded(this)) {
                startWishActivity();
            }
        }
        if (BuildConfig.DEBUG) LOGGER.debug("onClick({}) done", arg0);
    }

    private void startPlanActivity() {
        if (BuildConfig.DEBUG) LOGGER.debug("startPlanActivity()");

        PlanGrabber pg = new PlanGrabber(this, this,
                "http://www.metal-only.de/botcon/mob.php?action=plan");
        pg.start();
        if (BuildConfig.DEBUG) LOGGER.debug("startPlanActivity() done");

    }

    private void startWishActivity() {
        if (BuildConfig.DEBUG) LOGGER.debug("startWishActivity()");

        WishChecker wishChecker = new WishChecker(this, WishActivity.URL_WISHES);
        wishChecker.setOnWishesCheckedListener(new OnWishesCheckedListener() {

            @Override
            public void onWishesChecked(AllowedActions allowedActions) {
                if (allowedActions.moderated) {
                    if (allowedActions.wishes || allowedActions.regards) {
                        // allowedActions.wishes = false;
                        // allowedActions.regards = false;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(WishActivity.KEY_WISHES_ALLOWED,
                                allowedActions.wishes);
                        bundle.putBoolean(WishActivity.KEY_REGARDS_ALLOWED,
                                allowedActions.regards);
                        bundle.putString(WishActivity.KEY_NUMBER_OF_WISHES,
                                allowedActions.limit);
                        Intent wishIntent = new Intent(mainActivity,
                                WishActivity.class);
                        wishIntent.putExtras(bundle);
                        mainActivity.startActivity(wishIntent);
                    } else {
                        alertMessage(mainActivity, mainActivity
                                .getString(R.string.no_wishes_and_regards));
                    }
                } else {
                    alertMessage(mainActivity,
                            mainActivity.getString(R.string.no_moderator));
                }
            }
        });
        wishChecker.start();
        if (BuildConfig.DEBUG) LOGGER.debug("startWishActivity() done");
    }

    /**
     * sets listening to true sends start intent to PlayerService shows
     * connecting dialog
     */
    private void startListening() {
        if (BuildConfig.DEBUG) LOGGER.debug("startListening()");

        setSupportProgressBarIndeterminateVisibility(true);
        setShouldPlay(true);
        toggleStreamButton(true);
        Intent tmpIntent = new Intent(PlayerService.INTENT_PLAY);
        sendBroadcast(tmpIntent);
        if (BuildConfig.DEBUG) LOGGER.debug("startListening()");
    }

    /**
     * sets listening to false sends stop intent to PlayerService
     */
    public void stopListening() {
        if (BuildConfig.DEBUG) LOGGER.debug("stopListening()");

        setSupportProgressBarIndeterminateVisibility(false);
        setShouldPlay(false);
        clearMetadata();
        toggleStreamButton(false);
        Intent tmpIntent = new Intent(PlayerService.INTENT_STOP);
        sendBroadcast(tmpIntent);
        if (BuildConfig.DEBUG) LOGGER.debug("stopListening() done");


    }

    /**
     * displays meta data
     */
    public void displayMetadata() {
        if (BuildConfig.DEBUG) LOGGER.debug("displayMetadata()");

        if (getMetadataParser().toSong().isValid() && isShouldPlay()) {
            marqueeGenre.setText(getMetadataParser().getGENRE());
            marqueeMod.setText(getMetadataParser().getMODERATOR());
        }
        if (BuildConfig.DEBUG) LOGGER.debug("displayMetadata() done");

    }

    /**
     * clears the metadata display
     */
    private void clearMetadata() {
        if (BuildConfig.DEBUG) LOGGER.debug("clearMetadata()");

        setMetadataParser(new MetadataParser("-"));
        if (BuildConfig.DEBUG) LOGGER.debug("clearMetadata() done");
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (BuildConfig.DEBUG) LOGGER.debug("onItemClick(...)");

        final int index = arg2;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.history_options_array,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleAction(historySaver.size() - index - 1, which);
                    }
                }
        );
        builder.show();
        if (BuildConfig.DEBUG) LOGGER.debug("onItemClick(...) done");
    }

    /**
     * handles an action on an index
     *
     * @param index  item to handle
     * @param action action to handle
     */
    private void handleAction(final int index, int action) {
        if (BuildConfig.DEBUG) LOGGER.debug("handleAction({},{})", index, action);

        switch (action) {
            case 0: // add to favorites
                Song song = historySaver.get(index);
                if (favoritesSaver.isAlreadyIn(song) == -1) {
                    song.clearThumb();
                    favoritesSaver.addSong(song);
                    Toast.makeText(this, R.string.fav_added, Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(this, R.string.fav_already_in, Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case 1: // YouTube
                String searchStr = historySaver.get(index).interpret + " - "
                        + historySaver.get(index).title;
                try {
                    searchStr = URLEncoder.encode(searchStr, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Uri url = Uri.parse("http://www.youtube.com/results?search_query="
                        + searchStr);
                Intent youtube = new Intent(Intent.ACTION_VIEW, url);
                startActivity(youtube);
                break;
            case 2: // share
                String message = historySaver.get(index).interpret + " - "
                        + historySaver.get(index).title;
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, getResources()
                        .getStringArray(R.array.favorite_options_array)[2]));
                break;
        }
        if (BuildConfig.DEBUG) LOGGER.debug("handleAction({},{}) done", index, action);

    }


    /**
     * @param context
     * @param msg
     */
    public static void toastMessage(final Context context, final String msg) {
        if (BuildConfig.DEBUG) LOGGER.debug("toastMessage({},{})", context, msg);

        (new Handler(context.getMainLooper())).post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
        if (BuildConfig.DEBUG) LOGGER.debug("toastMessage({},{}) done", context, msg);
    }

    /**
     * @param context
     * @param msg
     */
    public static void alertMessage(final Context context, final String msg) {
        if (BuildConfig.DEBUG) LOGGER.debug("alertMessage({},{})", context, msg);

        (new Handler(context.getMainLooper())).post(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setMessage(msg);
                alert.setPositiveButton(context.getString(R.string.ok), null);
                alert.show();
            }
        });
        if (BuildConfig.DEBUG) LOGGER.debug("alertMessage({},{}) done", context, msg);
    }

    /**
     * converts timeMillis to a printable string
     *
     * @param timeMillis date as long
     * @return date as string
     */
    @SuppressLint("SimpleDateFormat")
    public static String longToDateString(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm-dd.MM.yy");
        return sdf.format(calendar.getTime());
    }

    public boolean isShouldPlay() {
        return shouldPlay;
    }

    public void setShouldPlay(boolean shouldPlay) {
        this.shouldPlay = shouldPlay;
    }

    public MetadataParser getMetadataParser() {
        return metadataParser;
    }

    public void setMetadataParser(MetadataParser metadataParser) {
        this.metadataParser = metadataParser;
    }

}
