package com.capstone.while1.beaconandroidstudio;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    static boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_login);
        if (!isLoggedIn) {
            //setContentView(R.layout.activity_login);

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

        }


//        setContentView(R.layout.activity_main);
        //setContentView(R.xml.pref_notification);
    }
}
