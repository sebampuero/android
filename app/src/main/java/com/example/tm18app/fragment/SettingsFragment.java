package com.example.tm18app.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import com.example.tm18app.R;

import me.pushy.sdk.Pushy;


/**
 * A simple {@link Fragment} subclass. Responsible for UI and events of the settings section.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = SettingsFragment.class.getName();
    public final static String SETTINGS_SHARED_PREFERENCES_FILE_NAME = TAG + ".SETTINGS_SHARED_PREFERENCES_FILE_NAME";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // set a name to these shared preferences to access them outside
        getPreferenceManager().setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES_FILE_NAME);
        setPreferencesFromResource(R.xml.settings_pref, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume(); // register the listener for changes when app in foreground
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause(); // unregister the listener for changes when app in background
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("notifications"))
            Pushy.toggleNotifications(!sharedPreferences.getBoolean(key, false), getActivity().getApplicationContext());
    }
}
