package com.capstone.while1.beaconandroidstudio;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

public class MainActivity extends AppCompatActivity {

    private static final int l33tHacks = 12345;
    static boolean isLoggedIn = false;
    NotificationCompat.Builder notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true); //deletes notification after u click on it
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//
//        fab.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
//
//                //placeholder for create event logic
//                final EditText eventName = (EditText) dialogView.findViewById(R.id.createEventName);
//                Button createButton = (Button) dialogView.findViewById(R.id.createEventButton);
//                createButton.setOnClickListener(new View.OnClickListener(){
//                    @Override
//                    public void onClick(View v) {
//                        eventName.setText("U SUCK AT HANZO SWITCH!");
//                    }
//                });
///*
//                //seekbar stuff for radius
//                final TextView textView = (TextView)dialogView.findViewById(R.id.createEventSeekBarText);
//                SeekBar seekBar = (SeekBar)dialogView.findViewById(R.id.createEventSeekBar);
//                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                    int progressValue = 0;
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        progressValue = progress + 1;
//                        textView.setText(" Radius (Miles): " + (progress + 1));
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                        //textView.setText(" Radius (Miles): " + progressValue);
//                    }
//                });
//*/
//
//
//                builder.setView(dialogView);
//
//                final AlertDialog dialog = builder.create();
//
//                //cancel button, closes add event dialog, not needed but nice to have
//                Button cancelButton = (Button) dialogView.findViewById(R.id.cancelEventButton);
//                cancelButton.setOnClickListener(new View.OnClickListener(){
//                    @Override
//                    public void onClick(View v) {
//                        dialog.dismiss();
//                    }
//                });
//
//
//
//                dialog.show();
//            }
//        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void debugPrint(String message) {
        Log.d("BeaconAndroidStudio", message);
    }

    public void onTestBtn(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_event, null);
        builder.setView(dialogView);

        Context context = getApplicationContext();
        final int white = ContextCompat.getColor(context, R.color.colorWhite);
        final int delRed = ContextCompat.getColor(context, R.color.deleteColor);

        Button deleteButton = (Button) dialogView.findViewById(R.id.deleteEventBtn);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button delBtn = (Button) dialogView.findViewById(R.id.deleteEventBtn);
                delBtn.setTextColor(white);
                delBtn.getBackground().setTint(delRed);
                delBtn.setText("Delete (Hold)");

                final DonutProgress donutProgress = (DonutProgress) dialogView.findViewById(R.id.deleteDonutProgress);
                donutProgress.setVisibility(View.VISIBLE);

                delBtn.setOnTouchListener(new View.OnTouchListener() {
                    private Handler progressHandler;
                    private DonutProgress delDonut;
                    private int progress = 0;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (delDonut == null) {
                            delDonut = donutProgress;
                        }
                        switch(event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (progressHandler != null) return true;
                                if (progress < 100) {
                                    progressHandler = new Handler();
                                    progressHandler.postDelayed(progressUp, 25);
                                } else {
                                    debugPrint("hey i'm at/past 100");
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                if (progressHandler == null) return true;
                                progressHandler.removeCallbacks(progressUp);
                                progressHandler = null;
                                // End
                                break;
                        }
                        return false;
                    }

                    Runnable progressUp = new Runnable() {
                        @Override public void run() {
                            if (progress < 100) {
                                delDonut.setDonut_progress((progress+=1) + "");
                                progressHandler.postDelayed(this, 25);
                            } else {
                                debugPrint("hey i'm in runnable at/past 100");
                                progressHandler.removeCallbacks(progressUp);
                                progressHandler = null;
                            }
                        }
                    };
                });
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
