package com.capstone.while1.beaconandroidstudio;

import android.Manifest;
import android.app.Dialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.app.Dialog;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.capstone.while1.beaconandroidstudio.beacondata.BeaconData;
import com.capstone.while1.beaconandroidstudio.beacondata.Event;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A placeholder fragment containing a simple view.
 */
@SuppressWarnings("MissingPermission")
public class MapFragment extends Fragment implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener, OnMyLocationButtonClickListener {

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
          
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

    public MapFragment() {
        mapFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.location_fragment, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
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
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15), 1500, null);
        }
    }

    public void createMarker(final Event event){
        //noinspection MissingPermission
        //Adds marker to map based on latitude and longitude parameters
        final Marker mark = googleMap.addMarker(new MarkerOptions().position(new LatLng(event.latitude, event.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        if (event.id == 0) {
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    ((MainActivity) MapFragment.this.getActivity()).onEditEvent(marker, event);
                    return true;
                }
            });
        } else {
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker arg0) {
                    //Creates dialog
                    final Dialog dialog = new Dialog(MapFragment.this.getActivity());
                    //Sets event title
                    //dialog.setTitle(title);

                    dialog.setContentView(R.layout.event_content_dialog);
                    //Sets event description
                    TextView titleTextView = (TextView) dialog.findViewById(R.id.eventTitle);
                    titleTextView.setTypeface(null, Typeface.BOLD);
                    titleTextView.setText(event.name);
                    //dialog.setTitle(title);
                    TextView descriptionTextView = (TextView) dialog.findViewById(R.id.eventDescription);
                    descriptionTextView.setText(event.description);
                    descriptionTextView.setMovementMethod(new ScrollingMovementMethod());
                    final TextView creatorPopularityTextView = (TextView) dialog.findViewById(R.id.eventCreatorAndPopularity);
                    creatorPopularityTextView.setText(/*"Created By: " + event.creatorId + */"\nPopularity: " + event.voteCount);


                    mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    final ImageButton up = (ImageButton) dialog.findViewById(R.id.upVoteBtn);
                    final ImageButton down = (ImageButton) dialog.findViewById(R.id.downVoteBtn);
                    final ImageButton closeBtn = (ImageButton) dialog.findViewById(R.id.closeBtn);
                    //final boolean[] upvote = {false, true, false};
                    //final boolean[] downvote = {false, true, false};
                    //User has clicked the "upvote button"
                    MapFragment.this.upvoteDownvoteListener(up, down);

                    //User clicked "cancel button"
                    closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    return true;
                }
            });
        }

    }

    private void upvoteDownvoteListener(final ImageButton up, final ImageButton down)
    {
        if (upvote) {
            up.setColorFilter(Color.GREEN);
            //text2.setText("Created By: " + creator + "\nPopularity: " + (popularity + 1));
        }
        if (downvote) {
            down.setColorFilter(Color.RED);
            //text2.setText("Created By: " + creator + "\nPopularity: " + (popularity - 1));
        }
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!upvote) {
                    up.setColorFilter(Color.GREEN);
                    down.setColorFilter(null);
                    upvote = true;
                    downvote = false;
                    //text2.setText("Created By: " + creator + "\nPopularity: " + (popularity + 1));
                    //Send upvote to DB
                    //Retract downvote from DB
                }
                //Upvote is "unvoted"
                else {
                    up.setColorFilter(null);
                    upvote = false;
                    //text2.setText("Created By: " + creator + "\nPopularity: " + (popularity));
                    //retract upvote from DB
                }
            }
        });

        //User clicked "downvote button"
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!downvote) {
                    down.setColorFilter(Color.RED);
                    up.setColorFilter(null);
                    downvote = true;
                    upvote = false;
                    //text2.setText("Created By: " + creator + "\nPopularity: " + (popularity - 1));
                    //Send downvote to DB
                    //Retract upvote from DB
                }
                //Downvote is "unvoted"
                else {
                    down.setColorFilter(null);
                    downvote = false;
                    //text2.setText("Created By: " + creator + "\nPopularity: " + (popularity));
                    //Retract downvote from DB
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
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onStop() {
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
        ArrayList<Event> events = BeaconData.getEvents();
        if (events != null) {
            for (Event event : events) {
                createMarker(event);
            }
        }
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
        updateMap(mCurrentLocation);
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
}
