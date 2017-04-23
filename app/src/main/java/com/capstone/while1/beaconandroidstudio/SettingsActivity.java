package com.capstone.while1.beaconandroidstudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;

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

                userEmail.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Activity settingsActivity = getActivity();
                        AlertDialog.Builder builder = new AlertDialog.Builder(settingsActivity);
                        final View dialogView = settingsActivity.getLayoutInflater().inflate(R.layout.dialog_change_email, null);
                        builder.setView(dialogView);
                        builder.create().show();
                        return true;
                    }
                });

                userPassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Activity settingsActivity = getActivity();
                        AlertDialog.Builder builder = new AlertDialog.Builder(settingsActivity);
                        final View dialogView = settingsActivity.getLayoutInflater().inflate(R.layout.dialog_change_password, null);
                        builder.setView(dialogView);
                        final AlertDialog dialog = builder.create();

                        //button/changepassword code


                        dialog.show();
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
