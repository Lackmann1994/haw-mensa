package de.haw_landshut.lackmann.haw_mensa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;

class SettingsUtils {

	static final String KEY_SOURCE_URL = "pref_source_url";
	private static final String KEY_STORAGE = "om_storage";
	
	static final String KEY_FAVOURITES = "pref_favourites";

    static final String KEY_STYLE = "pref_style";
    static final String KEY_FILTER = "pref_filter";

	private static Gson gson = new Gson();
	
    
    private static SharedPreferences getSharedPrefs(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    static String getSourceUrl(Context context) {
    	return getSharedPrefs(context).getString(KEY_SOURCE_URL, context.getResources().getString(R.string.source_url_default));
    }

    static Set<String> getFilter(Context context) {
        Set<String> activeFilters = new HashSet<>();
        return getSharedPrefs(context).getStringSet(KEY_FILTER, activeFilters);
    }
    
    static Storage getStorage(Context context) {
    	String json = getSharedPrefs(context).getString(KEY_STORAGE, "{}");
    	return gson.fromJson(json, Storage.class);
    }
    
    static void setStorage(Context context, Storage storage) {
    	String json = gson.toJson(storage);
    	SharedPreferences.Editor editor = getSharedPrefs(context).edit();
    	editor.putString(SettingsUtils.KEY_STORAGE, json);
    	editor.commit();
    }
    static int getThemeByString(String theme) {
        if (theme.equalsIgnoreCase("dark")) {
            return com.actionbarsherlock.R.style.Theme_Sherlock;
        } else if (theme.equalsIgnoreCase("light")) {
            return com.actionbarsherlock.R.style.Theme_Sherlock_Light;
        } else {
            Log.w(MainActivity.TAG, "Theme not found");
            return com.actionbarsherlock.R.style.Theme_Sherlock;
        }
    }


	static void updateFavouriteCanteensFromPreferences(Context context) {
        Set<String> favouriteCanteenKeys;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            favouriteCanteenKeys = getSharedPrefs(context).getStringSet(KEY_FAVOURITES, new HashSet<String>());
        } else {
            favouriteCanteenKeys = new HashSet(getStringArrayPref(context, KEY_FAVOURITES));
        }
		Log.d(MainActivity.TAG, String.format("Got favourites %s", favouriteCanteenKeys));
		Storage storage = getStorage(context);
		storage.setFavouriteCanteens(favouriteCanteenKeys);
		storage.saveToPreferences(context);
	}

    private static ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
}
