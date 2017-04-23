package com.capstone.while1.beaconandroidstudio;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.capstone.while1.beaconandroidstudio.beacondata.BeaconData;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //addPreferencesFromResource(R.xml.preferences);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }



    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

//            addPreferencesFromResource(R.xml.preferences);
        }

        public static class SettingsFragment extends PreferenceFragment {
            @Override
            public void onCreate(final Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.preferences);
                Preference logout = findPreference("logout");
                Preference userPassword = findPreference("userPassword");
                Preference userEmail = findPreference("userEmailAddress");

                userEmail.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        return false;
                    }
                });

                userPassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return true;
                    }
                });

                logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        PreferenceManager.getDefaultSharedPreferences(SettingsFragment.this.getActivity()
                                .getApplicationContext()).edit().remove(SavedPreferences.PREF_USER).apply();
                        BeaconData.deleteLoginInformation(getActivity());
                        SettingsFragment.this.startActivity(new Intent(getActivity(), SplashActivity.class));
                        return false;
                    }
                });
            }
        }
    }
}
