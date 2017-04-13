package com.capstone.while1.beaconandroidstudio;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.os.Bundle;
//import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
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
            }
        }

        @Override
        public void onOptionsMenuClosed(Menu menu) {
            SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
//            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = sp.edit();
            Log.v("abc", String.valueOf(R.id.wifiBool));
            Log.v("abc", String.valueOf(R.id.eventRadius));
            Log.v("abc", String.valueOf(R.id.eventRefreshInterval));
            editor.putInt("wifiBool", R.id.wifiBool);
            editor.putFloat("eventRadius", R.id.eventRadius);
            editor.putFloat("eventRefreshInterval", R.id.eventRefreshInterval);
//            editor.putString("startingLocation", R.id.startingLocation);
            editor.apply();
        }



    }
}
