package com.capstone.while1.beaconandroidstudio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.capstone.while1.beaconandroidstudio.beacondata.BeaconConsumer;
import com.capstone.while1.beaconandroidstudio.beacondata.BeaconData;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // If BeaconData is not initialized, initiate it!
        if (! BeaconData.isQueueInitialized()) {
            BeaconData.initiateQueue(this);
        }

        final LoginActivity activity = this;

        // If already logged in, go to the map page immediately
        if (BeaconData.tryToLoadUserInfo(this)) {
            Log.e("here", "1");
            BeaconData.retrieveLoginToken(
                    new BeaconConsumer<Integer>() {
                         @Override
                         public void accept(Integer obj) {
                             Log.e("here 2", "2");
                             Intent intent = new Intent(activity, MainActivity.class);
                             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                             startActivity(intent);
                             finish();
                         }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            Log.e("Login", "Failed to login.");
                        }
                    }, this);
        }

        // Else, request a login
        Log.e("here 3", "3");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View v) {
        //hide keyboard when button is clicked
        MainActivity.closeKeyboard(this);

        final String username = ((EditText)findViewById(R.id.loginUserInput)).getText().toString();
        Log.e("username", username);
        final String password = ((EditText)findViewById(R.id.loginPassInput)).getText().toString();
        Log.e("password", password);
        final TextView messageOutput = (TextView) LoginActivity.this.findViewById(R.id.messageOutput);
        messageOutput.setText(""); //whether or not there was an error in previous method call, clear it when login button clicked

        if (username.equals("") || password.equals("")) {
            messageOutput.setText("All fields must be set.");
        } else {
            final Button loginButton = (Button) findViewById(R.id.loginButton);
            final TextView registerLink = (TextView) findViewById(R.id.registerPageLink);
            loginButton.setClickable(false); //don't let user spam login button
            registerLink.setClickable(false); //User should not be able to go to register at this time
            final ProgressBar loginSpinner = (ProgressBar) findViewById(R.id.loginSpinner);
            loginSpinner.setVisibility(View.VISIBLE);

            final LoginActivity loginActivity = this;

            /*if this runs forever user can't click button again unless they restart the app,
            or they could switch to register view and then back to login (which basically loads a brand new instance of the view)
             */
            BeaconData.isValidLogin(username, password,
                    new Runnable() {
                        @Override
                        public void run() {
                            // Success!
                            Log.i("isValidLogin", "Returned successfully");
                            BeaconData.registerLogin(LoginActivity.this, username, password);
                            BeaconData.retrieveLoginToken(
                                    new BeaconConsumer<Integer>() {
                                         @Override
                                         public void accept(Integer userId) {
                                             // Successfully retrieved the user id!
                                             // Successfully logged in and grabbed a token!
                                             Log.i("Success!", "Successfully Grabbed the Token!");

                                             // Now move on to the next screen!
                                             Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                             LoginActivity.this.startActivity(intent);
                                             loginSpinner.setVisibility(View.GONE);
                                             LoginActivity.this.finish();
                                         }
                                    },
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            // Failed to grab token
                                            Log.e("Network Error", "Failed to grab the token!");
                                        }
                                    }, loginActivity);
                        }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            // Failure!

                            messageOutput.setText("Incorrect username and or password.");
                            loginSpinner.setVisibility(View.GONE);
                            loginButton.setClickable(true);
                            registerLink.setClickable(true); //User should be able to go to register at this time
                        }
                    });

        }
    }

    /*
    EDGE CASE:
    What happens if the user clicks the register or login links while waiting for a response from the database?
    If this happens when the API server is running, what happens?

    When the API server isn't running, loading spinner keeps going. Switching to the register page and then coming back to login
    actually stops the spinner, but not because the process is killed.

    If you hit the back button on the android phone, it goes back to the exact instance/process with the spinner still spinning
    and assumingly the api call still going.


    EDGE CASE FIX:
    A 'gross' fix for this would be to finish() the activity before switching views,
    but this makes it so that you can't hit the back button to go back to login from the register view or vice versa.
    Then again... how often would users do this? When they go to register they don't need to go back to login quickly because
    they're registering and it logs in for them right after. And if they really need to navigate between login and register views,
    they can just use the links. This fix may prevent using the back button but at least there's no rogue activities running in the
    background wasting computing power.

    I did this fix for both views.
     */
    //goToLoginPage() is similar
    public void goToRegisterPage(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    //TODO: delete this/move to main activity
    public void notificationExample(View view) {
        final NotificationCompat.Builder notification;

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true); //deletes notification after u click on it

        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setTicker("This is the ticker");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("BeaconApp Test");
        notification.setContentText("New event: play with SANICS at speaker circle!");
        //i assumed show lights would have the lights on the android device flash or maybe the screen wakes up but nothing :( at least sound and vibrate are working
        notification.setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_VIBRATE);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        final Handler handler = new Handler();

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
}