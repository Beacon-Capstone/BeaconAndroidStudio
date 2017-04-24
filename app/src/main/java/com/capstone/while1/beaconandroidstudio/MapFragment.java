package com.capstone.while1.beaconandroidstudio;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.capstone.while1.beaconandroidstudio.beacondata.BeaconConsumer;
import com.capstone.while1.beaconandroidstudio.beacondata.BeaconData;
import com.capstone.while1.beaconandroidstudio.beacondata.Event;
import com.capstone.while1.beaconandroidstudio.beacondata.Voted;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static com.capstone.while1.beaconandroidstudio.R.id;
import static com.capstone.while1.beaconandroidstudio.R.layout;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

/**
 * A placeholder fragment containing a simple view.
 */
@SuppressWarnings("MissingPermission")
public class MapFragment extends Fragment implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener, OnMyLocationButtonClickListener {

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
          
    protected static final String TAG = "MainActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static MapFragment mapFragment;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation;
    protected boolean mRequestingLocationUpdates;
    private MapView mMapView;
    private GoogleMap googleMap;
    private boolean upvote;
    private boolean downvote;
    private Circle userCircle;
    private boolean isPaused = false;

    public MapFragment() {
        mapFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Stop Register", (new Long(System.currentTimeMillis()).toString()));
        View rootView = inflater.inflate(layout.location_fragment, container, false);
        mMapView = (MapView) rootView.findViewById(id.mapView);
        mMapView.onCreate(savedInstanceState);

        mRequestingLocationUpdates = true;

        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                if (!PermissionUtils.isLocationEnabled(getContext())
                        || ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    PermissionUtils.PermissionDeniedDialog
                            .newInstance(true).show(getFragmentManager(), "dialog");
                } else {
                    googleMap = mMap;
                    googleMap.setMyLocationEnabled(true);
                    googleMap.setMinZoomPreference(9);
                    googleMap.getUiSettings().setCompassEnabled(false);
                    googleMap.getUiSettings().setRotateGesturesEnabled(false);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                }
            }
        });

        return rootView;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void startLocationUpdates() {
        LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        mRequestingLocationUpdates = true;
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, MapFragment.this);

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                "location settings ");
                        try {
                            status.startResolutionForResult(MapFragment.this.getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        mRequestingLocationUpdates = false;
                }
                MapFragment.this.updateMap(mCurrentLocation);
            }
        });
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
                .setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    private void updateMap(Location location) {
        if (location != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            newUserCircle();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14), 1500, null);
        }
    }

    public void createMarker(final Event event){
        //noinspection MissingPermission
        //Adds marker to map based on latitude and longitude parameters
        final Marker mark = googleMap.addMarker(new MarkerOptions().position(new LatLng(event.latitude, event.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); //default marker color is blue
        mark.setTag(event);

        // If made by the current user, adjust the color of the marker to reflect that
        if (event.creatorId.equals(BeaconData.getCurrentUserId())) {
            mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }

        // Now establish the event listener
        // events not made by user (made by other users)
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Creates dialog
                Event eve = (Event) marker.getTag();

                if (eve.creatorId.equals(BeaconData.getCurrentUserId())) {
                    // The current user owns the event, so show the appropriate menu
                    ((MainActivity) MapFragment.this.getActivity()).onEditEvent(marker, eve);
                    return true;
                }
                else {
                    // The current user does not own the event, so show the other menu
                    final Dialog dialog = new Dialog(MapFragment.this.getActivity());
                    //Sets event title
                    //dialog.setTitle(title);

                    dialog.setContentView(R.layout.event_content_dialog);
                    //Sets event description
                    TextView titleTextView = (TextView) dialog.findViewById(R.id.eventTitle);
                    titleTextView.setTypeface(null, Typeface.BOLD);
                    titleTextView.setText(eve.name);
                    //dialog.setTitle(title);
                    TextView descriptionTextView = (TextView) dialog.findViewById(R.id.eventDescription);
                    descriptionTextView.setText(eve.description);
                    descriptionTextView.setMovementMethod(new ScrollingMovementMethod());
                    final TextView creatorPopularityTextView = (TextView) dialog.findViewById(R.id.eventCreatorAndPopularity);
                    creatorPopularityTextView.setText(/*"Created By: " + event.creatorId + */"Popularity: " + eve.voteCount);


                    //change color of marker to green to let user know they've already looked at it
                    //mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    final ImageButton up = (ImageButton) dialog.findViewById(R.id.upVoteBtn);
                    final ImageButton down = (ImageButton) dialog.findViewById(R.id.downVoteBtn);
                    final ImageButton closeBtn = (ImageButton) dialog.findViewById(R.id.closeBtn);
                    //final boolean[] upvote = {false, true, false};
                    //final boolean[] downvote = {false, true, false};
                    //User has clicked the "upvote button"
                    MapFragment.this.upvoteDownvoteListener(up, down, eve, creatorPopularityTextView);

                    //User clicked "close button"
                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    return true;
                }
            }
        });
    }

    private void upvoteDownvoteListener(final ImageButton up, final ImageButton down, final Event event, final TextView popularText)
    {
        if (BeaconData.getVoteDecisionForEvent(event.id) == Voted.FOR) {
            up.setColorFilter(GREEN);
            down.setColorFilter(null);
            //text2.setText("Created By: " + creator + "\nPopularity: " + (popularity + 1));
        }
        if (BeaconData.getVoteDecisionForEvent(event.id) == Voted.AGAINST) {
            up.setColorFilter(null);
            down.setColorFilter(RED);
            //text2.setText("Created By: " + creator + "\nPopularity: " + (popularity - 1));
        }

        // Basic algorithm
        // For up clicked
        // If voted on this event, then unvote and uncolor both buttons
        // Vote for up


        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean haveVotedForThisEvent = BeaconData.haveVotedForEvent(event.id);

                if (haveVotedForThisEvent) {
                    final boolean haveUpvotedEvent = BeaconData.getVoteDecisionForEvent(event.id) == Voted.FOR;
                    Log.i("up.click", "Already voted?: " + haveUpvotedEvent);

                    // If voted on this event, then unvote and uncolor both buttons
                    BeaconData.unvoteOnEvent(event.id,
                            new Runnable() {
                                @Override
                                public void run() {
                                    // On Success
                                    up.setColorFilter(null);
                                    down.setColorFilter(null);

                                    // Get the vote count
                                    Event e = BeaconData.getEvent(event.id);
                                    final Integer popularityCount = e.voteCount;
                                    popularText.setText(String.format("Popularity: %d", e.voteCount));

                                    if (! haveUpvotedEvent) {// Vote for event
                                        BeaconData.voteUpOnEvent(event.id,
                                                new BeaconConsumer<Integer>() {
                                                    @Override
                                                    public void accept(Integer currentEventVoteCount) {
                                                        // Successfully voted up on the event
                                                        up.setColorFilter(GREEN);
                                                        down.setColorFilter(null);
                                                        popularText.setText("Popularity: " + currentEventVoteCount);
                                                    }
                                                },
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Log.e("upvote.click", "Failed to upvote after unvote.");
                                                    }
                                                });
                                    }

                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("upvote.click", "Failed to unvote event.");
                                }
                            }
                    );
                }
                else {
                    // Just upvote for the event
                    BeaconData.voteUpOnEvent(event.id,
                            new BeaconConsumer<Integer>() {
                                @Override
                                public void accept(Integer currentEventVoteCount) {
                                    // Successfully voted up on the event
                                    up.setColorFilter(Color.GREEN);
                                    down.setColorFilter(null);
                                    popularText.setText("Popularity: " + currentEventVoteCount);
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("upvote.click", "Failed to upvote...");
                                }
                            });
                }
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean haveVotedForThisEvent = BeaconData.haveVotedForEvent(event.id);

                if (haveVotedForThisEvent) {
                    final boolean haveDownvotedEvent = BeaconData.getVoteDecisionForEvent(event.id) == Voted.AGAINST;

                    // If voted on this event, then unvote and uncolor both buttons
                    BeaconData.unvoteOnEvent(event.id,
                            new Runnable() {
                                @Override
                                public void run() {
                                    // On Success
                                    up.setColorFilter(null);
                                    down.setColorFilter(null);

                                    Event e = BeaconData.getEvent(event.id);
                                    popularText.setText("Popularity: " + e.voteCount);

                                    if (! haveDownvotedEvent) {
                                        // Downvote the event
                                        BeaconData.voteDownOnEvent(event.id,
                                                new BeaconConsumer<Integer>() {
                                                    @Override
                                                    public void accept(Integer currentEventVoteCount) {
                                                        // Successfully voted up on the event
                                                        up.setColorFilter(null);
                                                        down.setColorFilter(RED);
                                                        popularText.setText("Popularity: " + currentEventVoteCount);
                                                    }
                                                },
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Log.e("downvote.click", "Failed to downvote after unvote.");
                                                    }
                                                });
                                    }
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("downvote.click", "Failed to unvote event.");
                                }
                            }
                    );
                }
                else {
                    // Just upvote for the event
                    BeaconData.voteDownOnEvent(event.id,
                            new BeaconConsumer<Integer>() {
                                @Override
                                public void accept(Integer currentEventVoteCount) {
                                    // Successfully voted up on the event
                                    up.setColorFilter(null);
                                    down.setColorFilter(RED);
                                    popularText.setText("Popularity: " + currentEventVoteCount);
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("downvote.click", "Failed to downvote...");
                                }
                            });
                }
            }
        });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onStart(){
        super.onStart();
        if (!PermissionUtils.isLocationEnabled(getContext())
                || ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(true).show(getActivity().getSupportFragmentManager(), "dialog");
        } else
            mGoogleApiClient.connect();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(true).show(getFragmentManager(), "dialog");
        }
        updateMap(mCurrentLocation);
    }

    @Override
    public void onPause() {
        isPaused = true;

        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
//        if (mGoogleApiClient.isConnected()) {
//            stopLocationUpdates();
//        }
        MainActivity.userHasAppOpen = false;
    }

    @Override
    public void onStop() {
        isPaused = false;

        super.onStop();
        mGoogleApiClient.disconnect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(true).show(getFragmentManager(), "dialog");
        } else {
            if (mCurrentLocation == null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                updateMap(mCurrentLocation);
            }
            if (mRequestingLocationUpdates) {
                Log.i(TAG, "in onConnected(), starting location updates");
                startLocationUpdates();
            }
        }

        ////////////////////////////
        // Initiate BeaconData Class

        BeaconData.setEventUpdateHandler(new BeaconConsumer<ArrayList<Event>>() {
            @Override
            public void accept(ArrayList<Event> updatedEvents) {
                Log.i("Events Updated", "Starting to process update batch...");
                googleMap.clear();
                newUserCircle();

                ArrayList<Event> events = BeaconData.getEvents();
                if (events != null) {
                    for (int i = 0; i < events.size(); ++i) {
                        if (! events.get(i).deleted) {
                            createMarker(events.get(i));
                        }
                    }
                }
            }
        });

        // Initiate the BeaconData queue
        if (! BeaconData.isQueueInitialized()) {
            Log.i("MapFragment:onCreate...", "Initializing BeaconData queue");
            BeaconData.initiateQueue(getContext());
        }

        if (! BeaconData.areVotesLoaded()) {
            BeaconData.getEventsVotedFor(
                    new Runnable() {
                        @Override
                        public void run() {
                            // Success!
                            Log.i("getEventsVotedFor", "Successfully downloaded");
                        }
                    },
                    new BeaconConsumer<String>() {
                        @Override
                        public void accept(String err) {
                            // Failed...
                            Log.d("getEventsVotedFor", err.toString());
                        }
                    });
        }

        if (! BeaconData.areEventsDownloadedInitially()) {
            BeaconData.downloadAllEventsInArea(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       // Successfully downloaded all the events!
                                                       System.out.println("Downloaded all events!");
                                                       Log.i("Download Events", "Success!");

                                                       // Set a timer to update BeaconData periodically
                                                       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                                                       Timer timer = new Timer();
                                                       timer.schedule(new TimerTask() {
                                                           @Override
                                                           public void run() {
                                                               Double lat = mCurrentLocation.getLatitude();
                                                               Double lng = mCurrentLocation.getLongitude();
                                                               Log.i("Update", "Update Events Called at Location: " + lat + ", " + lng);
                                                               BeaconData.downloadUpdates((float)mCurrentLocation.getLatitude(), (float)mCurrentLocation.getLongitude());
                                                           }
                                                       }, 7500, Integer.parseInt(sp.getString("eventRefreshInterval", "5")) * 1000);

                                                   }
                                               }, (float)mCurrentLocation.getLatitude(),
                    (float)mCurrentLocation.getLongitude());
        }

        BeaconData.setOnInitialized(new Runnable() {
            @Override
            public void run() {
                // On initialized code goes here
                // The votes, events, and token are all available now...
                Log.i("setOnInitialized", "Function called");
            }
        });


        // End initiate BeaconData Class
        ////////////////////////////////

        final Handler handler = new Handler();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (! isPaused) {
                    Log.i("notificationHandler", "Handling notifications.");
                    Location currLocation = MapFragment.mapFragment.getCurrentLocation();
                    PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                    String radiusInMiles = sp.getString("EVENT_RADIUS", "1");
                    float radiusInMilesAsFloat = Float.parseFloat(radiusInMiles);
                    notification(handler, BeaconData.getEventsWithinDistance((float)radiusInMilesAsFloat * 1609, (float)currLocation.getLatitude(), (float)currLocation.getLongitude()).size());
                }
            }
        }, 0, 1000 * 5);
    }


    public void notification(Handler handler, int numEvents) {
        if (numEvents == 0) {
            return;
        }
        final NotificationCompat.Builder notification;

        notification = new NotificationCompat.Builder(this.getActivity());
        notification.setAutoCancel(true); //deletes notification after u click on it

        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Beacon Events");
        notification.setContentText("There are events in your area.");
        //i assumed show lights would have the lights on the android device flash or maybe the screen wakes up but nothing :( at least sound and vibrate are working
        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_VIBRATE);

        Intent intent = new Intent(this.getContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        final NotificationManager nm = (NotificationManager) this.getActivity().getSystemService(this.getActivity().NOTIFICATION_SERVICE);

        MainActivity.debugPrint("sending notification in 10 seconds...");
        //do notification after 10 seconds => tested and it works if the app is running in background
        // if they actually quit/close the app rather than just hitting home button or locking screen the notification does not appear
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nm.notify(MainActivity.l33tHacks, notification.build());
                MainActivity.debugPrint("SENT NOTIFICATION!");
            }
        }, 10000);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        newUserCircle();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public Location getCurrentLocation() {
        return this.mCurrentLocation;
    }

    public double milesToMeters(double miles) {
        return miles * 1609.34;
    }

    public void newUserCircle() {
        if (isUserCircleVisible())
            userCircle.remove();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String radius = sp.getString("eventRadius", "1");
        CircleOptions options = new CircleOptions()
                .center(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                .radius(milesToMeters(Double.parseDouble(radius)))
                .fillColor(0x309392F2)
                .strokeWidth(0);
        this.userCircle = googleMap.addCircle(options);
    }

    public boolean isUserCircleVisible() {
        return (this.userCircle != null);
    }
}
