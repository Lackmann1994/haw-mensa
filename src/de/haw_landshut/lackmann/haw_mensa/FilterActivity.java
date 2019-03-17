package de.haw_landshut.lackmann.haw_mensa;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

@SuppressLint("ExportedPreferenceActivity")
public class FilterActivity extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String prefStyle = prefs.getString(FilterUtils.KEY_STYLE, getString(R.string.pref_theme_default));
        setTheme(FilterUtils.getThemeByString(prefStyle));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
              .replace(android.R.id.content, new FilterFragment())
              .commit();
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(FilterUtils.KEY_STYLE)) {
            recreate();
        }
    }
}