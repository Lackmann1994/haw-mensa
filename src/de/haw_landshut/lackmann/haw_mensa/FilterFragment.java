package de.haw_landshut.lackmann.haw_mensa;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.ArrayList;

public class FilterFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.filter);

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        EditTextPreference editTextPref = (EditTextPreference) findPreference(FilterUtils.KEY_SOURCE_URL);
//        editTextPref.setSummary(sp.getString(FilterUtils.KEY_SOURCE_URL, editTextPref.getText()));

        ListPreference themePref = (ListPreference) findPreference(FilterUtils.KEY_STYLE);
//        themePref.setSummary(themePref.getEntry());

//        updateFavouriteCanteensSummary();
    }

    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            pref.setSummary(etp.getText());
        }

        if (key.equals(FilterUtils.KEY_FAVOURITES)) {
            updateFavouriteCanteensSummary();
        }
    }

    private void updateFavouriteCanteensSummary() {
        FilterUtils.updateFavouriteCanteensFromPreferences(MainActivity.getAppContext());
        ArrayList<Canteen> favouriteCanteens = FilterUtils.getStorage(MainActivity.getAppContext()).getFavouriteCanteens();
        Preference pref = findPreference(FilterUtils.KEY_FAVOURITES);
        int size = favouriteCanteens.size();
        if (size == 0) {
            pref.setSummary(getString(R.string.canteen_desc_empty));
        } else {
            pref.setSummary(String.format(getString(R.string.canteen_desc), size));
        }
    }
}