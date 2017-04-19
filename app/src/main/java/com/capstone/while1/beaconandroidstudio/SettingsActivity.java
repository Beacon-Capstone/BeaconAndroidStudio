package com.capstone.while1.beaconandroidstudio;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
//import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;

//import static com.capstone.while1.beaconandroidstudio.R.id.logout;

public class SettingsActivity extends PreferenceActivity{
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
                logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        System.out.println("asdfasdfasdfd");
                        return false;
                    }
                });
            }
        }
    }
}
