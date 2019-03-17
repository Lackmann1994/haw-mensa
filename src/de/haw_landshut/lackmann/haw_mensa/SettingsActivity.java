package de.haw_landshut.lackmann.haw_mensa;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.actionbarsherlock.app.SherlockPreferenceActivity;


@SuppressLint("ExportedPreferenceActivity")
public class SettingsActivity extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String prefStyle = prefs.getString(SettingsUtils.KEY_STYLE, getString(R.string.pref_theme_default));
        setTheme(SettingsUtils.getThemeByString(prefStyle));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
              .replace(android.R.id.content, new SettingsFragment())
              .commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(SettingsUtils.KEY_STYLE)) {
            recreate();
        }
    }
}