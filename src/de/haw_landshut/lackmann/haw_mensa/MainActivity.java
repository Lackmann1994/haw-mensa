package de.haw_landshut.lackmann.haw_mensa;

import android.annotation.SuppressLint;


import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@SuppressLint("NewApi")
public class MainActivity extends SherlockFragmentActivity implements
        OnNavigationListener, OnFinishedFetchingCanteensListener, OnFinishedFetchingDaysJsonListener, OnFinishedFetchingDaysCSVListener {

    public static final String TAG = "Canteendroid";
    public static final Boolean LOGV = true;
    static Storage storage;
    private ArrayList<SpinnerItem> spinnerItems;
    private static LocationManager locationManager;

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    OnSharedPreferenceChangeListener listener;
    SectionsPagerAdapter sectionsPagerAdapter;
    static CustomViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String prefStyle = prefs.getString(SettingsUtils.KEY_STYLE, getString(R.string.pref_theme_default));
        setTheme(SettingsUtils.getThemeByString(prefStyle));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        storage = SettingsUtils.getStorage(context);
        createSectionsPageAdapter();

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case SettingsUtils.KEY_FAVOURITES:
                        refreshFavouriteCanteens();
                        break;
                    case SettingsUtils.KEY_SOURCE_URL:
                        reload();
                        break;
                    case SettingsUtils.KEY_STYLE:
                        recreate();
                        break;
                }
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);
        spinnerItems = new ArrayList<>();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_launcher);
        actionBar.setTitle(R.string.title_activity_main);
        actionBar.setHomeButtonEnabled(true);
        reload();
        refreshFavouriteCanteens();
    }

    @Override
    public void onPause() {
        super.onPause();
        storage.saveToPreferences(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        storage.loadFromPreferences(context);
        updateMealStorage();
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    public static LocationManager getLocationManager() {
        return MainActivity.locationManager;
    }

    private void createSectionsPageAdapter() {
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = (CustomViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                viewPager.setCurrentItem(0);
                break;
            case Calendar.TUESDAY:
                viewPager.setCurrentItem(1);
                break;
            case Calendar.WEDNESDAY:
                viewPager.setCurrentItem(2);
                break;
            case Calendar.THURSDAY:
                viewPager.setCurrentItem(3);
                break;
            case Calendar.FRIDAY:
                viewPager.setCurrentItem(4);
                break;
            default:
                viewPager.setCurrentItem(5);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Save state, flushed cache storage");
        outState.putInt("page", viewPager.getCurrentItem());
        storage.saveToPreferences(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        Log.d(TAG, "Restore state");
        viewPager.setCurrentItem(savedState.getInt("page"));
    }

    public void changeCanteenTo(Canteen canteen) {
        if (storage.getCurrentCanteen().key.compareTo(canteen.key) == 0)
            return;
        storage.setCurrentCanteen(canteen);
        storage.saveToPreferences(this);

        updateMealStorage();
        sectionsPagerAdapter.notifyDataSetChanged();
    }

    public void updateMealStorage() {
        updateMealStorage(false);
    }


    public void updateMealStorage(Boolean force) {

        Day day = new Day("");

        Canteen canteen = storage.getCurrentCanteen();
//        canteen = storage.canteen;


        Log.d(MainActivity.TAG, "Meal cache miss");
        if (canteen == null)
            return;

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Boolean startedFetching = false;

        ArrayList<Integer> sections = sectionsPagerAdapter.getDaySections();
        int i = 0;
        for (Integer position : sections) {
            cal.setTime(now);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            //skip weekend
            if (i == 5) i = 7;
            cal.add(Calendar.DAY_OF_YEAR, i++);
            int week = cal.get(Calendar.WEEK_OF_YEAR);
            String dateString = df.format(cal.getTime());

            DayFragment fragment = (DayFragment) sectionsPagerAdapter.getItem(position);
            fragment.setDate(df.format(cal.getTime()));

            cal.add(Calendar.DAY_OF_YEAR, i + 6);
            String dateString2 = df.format(cal.getTime());
            if (startedFetching) {
                fragment.setToFetching(true, !fragment.isListShown());
                canteen.justUpdated(dateString);
                continue;
            }

            if (day == null || canteen.isOutOfDate(dateString) || force) {
                if (day == null) {
                    Log.d(MainActivity.TAG, "Meal cache miss");
                } else if (canteen.isOutOfDate(dateString)) {
                    Log.d(MainActivity.TAG, "Out of date");
                } else if (force) {
                    Log.d(MainActivity.TAG, "Forced update");
                }

                if (isOnline(this)) {
                    if (day == null) {
                        Days newDay = new Days();
                        Day nullDay = new Day(dateString);
                        newDay.add(nullDay);
                        canteen.updateDays(newDay);
                    }
                    fragment.setToFetching(true, !fragment.isListShown());
                    String baseUrl = SettingsUtils.getSourceUrl(MainActivity.context);
                    String url = baseUrl + "canteens/" + canteen.key + "/meals/?start=" + dateString;
                    String url2 = baseUrl + "canteens/" + canteen.key + "/meals/?start=" + dateString2;

                    if (url.startsWith("https://stwno.de")) {
                        RetrieveFeedTask task = new RetrieveDaysFeedTaskCSV(MainActivity.context, this, this, canteen, dateString);
                        task.execute(new String[]{
                                baseUrl +"/"+ week + ".csv?t=1552396597",
                                baseUrl +"/"+ (week + 1) + ".csv?t=1552396597"
                        });
                    } else {
                        RetrieveFeedTask task = new RetrieveDaysFeedTaskCSV(MainActivity.context, this, this, canteen, dateString);
                        task.execute(new String[]{url, url2});
                    }
                    startedFetching = true;

                } else {
                    Toast.makeText(getApplicationContext(), R.string.noconnection, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(MainActivity.TAG, "Meal cache hit");
                fragment.setToFetching(false, !fragment.isListShown());
            }
        }
    }

    @Override
    public void onDaysFetchFinished(RetrieveDaysFeedJsonTask task) {
        sectionsPagerAdapter.setToFetching(false, false);
        task.canteen.updateDays(task.getDays());
        sectionsPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDaysFetchFinished(RetrieveDaysFeedTaskCSV task) {
        sectionsPagerAdapter.setToFetching(false, false);
        task.canteen.updateDays(task.getDays());
        sectionsPagerAdapter.notifyDataSetChanged();
    }

    /**
     * Refreshes the canteens in the action bar
     * <p>
     * TODO: should wait for completion of refreshAvailableCanteens()
     */
    private void refreshFavouriteCanteens() {
        Log.d(TAG, "Refreshing favourite canteen list");

        SettingsUtils.updateFavouriteCanteensFromPreferences(context);


        storage.loadFromPreferences(context);
        spinnerItems.clear();
        spinnerItems.addAll(storage.getFavouriteCanteens());


        if (spinnerItems.size() == 0 && !storage.getCanteens(this).isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.noactivecanteens)
                    .setMessage(R.string.chooseone)
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNeutralButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Intent settings = new Intent(
                                            MainActivity.context,
                                            SettingsActivity.class);
                                    startActivity(settings);
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        Log.d(TAG, String.format("Spinner items: %s", spinnerItems));


        ActionBar actionBar = getSupportActionBar();
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<SpinnerItem>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        actionBar.setListNavigationCallbacks(spinnerAdapter, this);

        Canteen curr = storage.getCurrentCanteen();
        if (curr != null) {
            Log.d(TAG, curr.toString());
        }
    }

    private void refreshAvailableCanteens() {
        String baseUrl = SettingsUtils.getSourceUrl(this);
        String url = "http://openmensa.org/api/v2/" + "canteens" + "?limit=50";

        RetrieveFeedTask task = new RetrieveCanteenFeedTask(this, this, this, url);
        task.execute(url);
    }

    @Override
    public void onCanteenFetchFinished(RetrieveCanteenFeedTask task) {
        storage.saveCanteens(this, task.getCanteens());

        refreshFavouriteCanteens();
        updateMealStorage();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        SpinnerItem item = spinnerItems.get(itemPosition);
        return item.execute(this, itemPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Calendar cal = Calendar.getInstance();
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY:
                        viewPager.setCurrentItem(0);
                        break;
                    case Calendar.TUESDAY:
                        viewPager.setCurrentItem(1);
                        break;
                    case Calendar.WEDNESDAY:
                        viewPager.setCurrentItem(2);
                        break;
                    case Calendar.THURSDAY:
                        viewPager.setCurrentItem(3);
                        break;
                    case Calendar.FRIDAY:
                        viewPager.setCurrentItem(4);
                        break;
                    default:
                        viewPager.setCurrentItem(5);
                }
                return true;
            case R.id.menu_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.filter:
                Intent filter = new Intent(this, FilterActivity.class);
                startActivity(filter);
                return true;
            case R.id.reload:
                reload(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static boolean isOnline(Context context) {

        NetworkInfo info = ((ConnectivityManager) Objects.requireNonNull(context
                .getSystemService(Context.CONNECTIVITY_SERVICE)))
                .getActiveNetworkInfo();

        if (info == null || !info.isConnected()) {
            return false;
        }
        return !info.isRoaming();
    }

    private void reload() {
        reload(false);
    }


    private void reload(boolean force) {
        storage.loadFromPreferences(this);

        if (isOnline(MainActivity.this)) {
            if (storage.areCanteensOutOfDate() || storage.isEmpty() || force) {
                Log.d(TAG, "Fetch canteens because storage is out of date or empty");
                refreshAvailableCanteens();
            }
            updateMealStorage(force);
        } else {
            if (force) {
                new AlertDialog.Builder(MainActivity.this).setNegativeButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).setTitle(R.string.noconnection).setMessage(R.string.pleaseconnect).create().show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.noconnection, Toast.LENGTH_LONG).show();
            }
        }
    }
}
