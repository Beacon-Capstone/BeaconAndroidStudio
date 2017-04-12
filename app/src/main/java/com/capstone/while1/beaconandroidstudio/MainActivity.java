package com.capstone.while1.beaconandroidstudio;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.view.View.OnClickListener;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    static boolean isLoggedIn = false;
    NotificationCompat.Builder notification;
    private static final int l33tHacks = 12345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_login);

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true); //deletes notification after u click on it

        if (!isLoggedIn) {
            //setContentView(R.layout.activity_login);

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
            addListenerOnButton();
//        setContentView(R.layout.activity_main);

//this causes the app to crash so I commented out when I merged homerLogin
		//setContentView(R.xml.pref_notification);
    }

    public void notificationExample(View view) {
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("BeaconApp Test");
        notification.setContentText("New event: play with SANICS at speaker circle!");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(l33tHacks, notification.build());
    }

    //ImageButton myButton = (ImageButton) findViewById(R.id.imageButton1);

    public void addListenerOnButton() {
        ImageButton myButton = (ImageButton) findViewById(R.id.imageButton1);
        myButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent
                        (getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}
