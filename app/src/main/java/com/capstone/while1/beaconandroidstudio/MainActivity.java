package com.capstone.while1.beaconandroidstudio;


import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;

import com.capstone.while1.beaconandroidstudio.beacondata.BeaconData;
import com.capstone.while1.beaconandroidstudio.beacondata.Event;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.maps.model.Marker;

public class MainActivity extends AppCompatActivity {
    private static final int l33tHacks = 12345;
    NotificationCompat.Builder notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true); //deletes notification after u click on it

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.onAddEvent(view);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void debugPrint(String message) {
        Log.d("BeaconAndroidStudio", message);
    }

    public void onAddEvent(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        //placeholder for create event logic
        final EditText eventName = (EditText) dialogView.findViewById(R.id.createEventName);
        final EditText eventDescription = (EditText) dialogView.findViewById(R.id.createEventDescription);

        Button createButton = (Button) dialogView.findViewById(R.id.createEventButton);
        createButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = eventName.getText().toString();
                String description = eventDescription.getText().toString();

                // both title and description need something in their fields, don't even close dialog if one of the fields is empty (need to add more error handling (prevent whitespace characters only etc.)
                if (!title.equals("") && !description.equals("")) {
                    dialog.dismiss();
                    if (MapFragment.mapFragment != null) {
                        MapFragment mFrag = MapFragment.mapFragment;
                        Location currLocation = mFrag.getCurrentLocation();
                        Double latitude = currLocation.getLatitude();
                        Double longitude = currLocation.getLongitude();
                        //store in eventList and save list, save list as json in savedPreferences
                        BeaconData.createEvent(title, description, latitude, longitude);
                        MainActivity.this.debugPrint("success! mapPinCreated!");
                    } else {
                        MainActivity.this.debugPrint("mapFragment is NULL");
                    }
                }
            }
        });
/*
                //seekbar stuff for radius
                final TextView textView = (TextView)dialogView.findViewById(R.id.createEventSeekBarText);
                SeekBar seekBar = (SeekBar)dialogView.findViewById(R.id.createEventSeekBar);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    int progressValue = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progressValue = progress + 1;
                        textView.setText(" Radius (Miles): " + (progress + 1));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //textView.setText(" Radius (Miles): " + progressValue);
                    }
                });
*/

        //cancel button, closes add event dialog, not needed but nice to have
        Button cancelButton = (Button) dialogView.findViewById(R.id.cancelEventButton);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });



        dialog.show();
    }

    public void onEditEvent(final Marker marker, final Event event) {
        //create alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_event, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        //button/input logic
        final Context context = getApplicationContext();
        final int white = ContextCompat.getColor(context, R.color.colorWhite);
        final int delRed = ContextCompat.getColor(context, R.color.deleteColor);

        final EditText eventTitle = (EditText) dialogView.findViewById(R.id.editEventName);
        final EditText eventDescription = (EditText) dialogView.findViewById(R.id.editEventDescription);
        final TextView eventPopularity = (TextView) dialogView.findViewById(R.id.editEventPopularity);

        //set current details of event
        eventTitle.setText(event.name);
        eventDescription.setText(event.description);
        eventPopularity.setText("Popularity: " + event.voteCount);

        Button saveButton = (Button) dialogView.findViewById(R.id.saveEditEventBtn);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //update user's own event on device
                //save user made event as 2 strings (name and description)
                String newTitle = eventTitle.getText().toString();
                String newDescription = eventDescription.getText().toString();

                if (!newTitle.equals("") && !newDescription.equals("")) {
                    event.name = newTitle;
                    event.description = newDescription;
                    BeaconData.updateEvent(event);
                    //!!!!need code for updating event in database
                    dialog.dismiss();
                }
            }
        });

        Button cancelButton = (Button) dialogView.findViewById(R.id.cancelEditEventBtn);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //user doesn't want to save changes, close dialog
                dialog.dismiss();
            }
        });


        final Button deleteButton = (Button) dialogView.findViewById(R.id.deleteEventBtn);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton.setTextColor(white);
                deleteButton.getBackground().setTint(delRed);
                deleteButton.setText("Delete (Hold)");

                final DonutProgress donutProgress = (DonutProgress) dialogView.findViewById(R.id.deleteDonutProgress);
                donutProgress.setVisibility(View.VISIBLE);

                deleteButton.setOnTouchListener(new View.OnTouchListener() {
                    private Handler progressHandler;
                    private DonutProgress delDonut;
                    private int progress = 0;
                    Runnable progressUp = new Runnable() {
                        @Override
                        public void run() {
                            if (progress < 100) {
                                delDonut.setDonut_progress((progress += 1) + "");
                                progressHandler.postDelayed(this, 1);
                            } else {
                                debugPrint("hey i'm in runnable at/past 100");
                                progressHandler.removeCallbacks(progressUp);
                                progressHandler = null;
                                //delete event function call
                                deleteEvent(marker, event.id);
                                dialog.dismiss();
                            }
                        }
                    };

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
                                    progressHandler.postDelayed(progressUp, 1);
                                } else {
                                    debugPrint("hey i'm at/past 100");
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                if (progressHandler == null) return true;
                                if (progress < 100) {
                                    delDonut.setDonut_progress((progress = 0) + "");
                                }
                                progressHandler.removeCallbacks(progressUp);
                                progressHandler = null;
                                if (progress >= 100) {
                                    //delete event in database and on local device
                                    dialog.dismiss();
                                }
                                // End
                                break;
                        }
                        return false;
                    }

                    void deleteEvent(Marker marker, int eventId) {
                        BeaconData.deleteEvent(eventId);
                        marker.remove();
                    }
                });
            }
        });

        //display dialog
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

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(true).show(getSupportFragmentManager(), "dialog");
        }
    }
}
