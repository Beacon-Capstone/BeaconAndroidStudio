package com.capstone.while1.beaconandroidstudio;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobilehelper.auth.DefaultSignInResultHandler;
import com.amazonaws.mobilehelper.auth.IdentityManager;
import com.amazonaws.mobilehelper.auth.IdentityProvider;
import com.amazonaws.mobilehelper.auth.StartupAuthErrorDetails;
import com.amazonaws.mobilehelper.auth.StartupAuthResult;
import com.amazonaws.mobilehelper.auth.StartupAuthResultHandler;

public class SplashActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback {
    public final static String TAG = "Splash";
    /**
     * The code used when requesting permissions
     */
    private static final int PERMISSIONS_REQUEST = 1234;
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "I am here!");

        /*
          On a post-Android 6.0 devices, check if the required permissions have
          been granted.
         */
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, PERMISSIONS_REQUEST, android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            AWSMobileClient.initializeMobileClientIfNecessary(getApplicationContext());
            final IdentityManager identityManager =
                    AWSMobileClient.defaultMobileClient().getIdentityManager();

            identityManager.doStartupAuth(this,
                    new StartupAuthResultHandler() {
                        @Override
                        public void onComplete(final StartupAuthResult authResults) {
                            if (authResults.isUserSignedIn()) {
                                final IdentityProvider provider = identityManager.getCurrentIdentityProvider();
                                // If we were signed in previously with a provider indicate that to the user with a toast.
                            } else {
                                // Either the user has never signed in with a provider before or refresh failed with a previously
                                // signed in provider.

                                // Optionally, you may want to check if refresh failed for the previously signed in provider.
                                final StartupAuthErrorDetails errors = authResults.getErrorDetails();
                                if (errors.didErrorOccurRefreshingProvider()) {
                                    Log.w("Error", String.format(
                                            "Credentials for previously signed-in provider could not be refreshed."));
                                }

                                doMandatorySignIn(identityManager);
                                return;
                            }

                            // Go to your main activity and finish your splash activity here
                            goMain(SplashActivity.this);
                        }
                    }, 2000);
        }
    }

    private void doMandatorySignIn(final IdentityManager identityManager) {
        identityManager.signInOrSignUp(SplashActivity.this, new DefaultSignInResultHandler() {
            @Override
            public void onSuccess(Activity callingActivity, IdentityProvider provider) {
                if (provider != null) {
                    Log.d("Error", String.format("User sign-in with %s provider succeeded",
                            provider.getDisplayName()));
                }
                goMain(callingActivity);
            }

            @Override
            public boolean onCancel(Activity callingActivity) {
                return false;
            }
        });
        SplashActivity.this.finish();
    }

    /**
     * Go to the main activity.
     */
    private void goMain(final Activity callingActivity) {
        callingActivity.startActivity(new Intent(callingActivity, MainActivity.class));
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Enable the map layer if the permission has been granted.
                goMain(SplashActivity.this);
            } else {
                mPermissionDenied = true;
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        Log.i(TAG, "Here");
        super.onResumeFragments();
        if (mPermissionDenied) {
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}
