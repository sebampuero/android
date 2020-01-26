package com.example.tm18app.fragment;


import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.tm18app.MainActivity;
import com.example.tm18app.R;
import com.example.tm18app.constants.Constant;
import com.example.tm18app.util.DialogManager;

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
    public final static String SETTINGS_SHARED_PREFERENCES_FILE_NAME = TAG +
            ".SETTINGS_SHARED_PREFERENCES_FILE_NAME";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // set a mName to these shared preferences to access them outside
        getPreferenceManager().setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES_FILE_NAME);
        setPreferencesFromResource(R.xml.settings_pref, rootKey);
        setupViews();
        Preference button = findPreference("logout");
        button.setOnPreferenceClickListener(preference -> {
            DialogManager.
                    getInstance()
                    .showAlertDialogSingleButton(requireContext(),
                            requireContext().getString(R.string.log_out_alert_title),
                            requireContext().getString(R.string.log_out_conf_message),
                            android.R.string.yes,
                            (dialogInterface, i) -> {
                                SharedPreferences preferences = requireContext().
                                        getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
                                SharedPreferences.Editor e = preferences.edit();
                                e.clear().apply(); // clear SharedPreferences info
                                Pushy.unregister(requireContext()); // wipe user token and auth key info
                                // Navigate to the main fragment with a deep link
                                PendingIntent pendingIntent = new NavDeepLinkBuilder(requireContext())
                                        .setComponentName(MainActivity.class)
                                        .setGraph(R.navigation.nav_graph)
                                        .setDestination(R.id.mainFragment)
                                        .createPendingIntent();
                                try {
                                    pendingIntent.send();
                                } catch (PendingIntent.CanceledException ex) {
                                    ex.printStackTrace();
                                }
                            });
            return true;
        });
    }

    private void setupViews() {
        Toolbar toolbar = ((MainActivity)requireActivity()).getToolbar();
        toolbar.getMenu().clear();
    }

    @Override
    public void onResume() {
        super.onResume(); // register the listener for changes when app in foreground
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        getPreferenceScreen().findPreference("notifications_other")
                .setEnabled(!getContext()
                        .getSharedPreferences(SETTINGS_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
                        .getBoolean("notifications", true));
    }

    @Override
    public void onPause() {
        super.onPause(); // unregister the listener for changes when app in background
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("notifications")){ // upon toggle notifs off, pushy shuts down service
            Pushy.toggleNotifications(!sharedPreferences.getBoolean(key, false),
                    requireActivity().getApplicationContext());
            getPreferenceScreen().findPreference("notifications_other")
                    .setEnabled(!sharedPreferences.getBoolean(key, false));
        }
    }
}
