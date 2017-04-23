package com.capstone.while1.beaconandroidstudio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
            BeaconData.retrieveLoginToken(
                    new BeaconConsumer<Integer>() {
                         @Override
                         public void accept(Integer obj) {
                             Intent intent = new Intent(activity, MainActivity.class);
                             startActivity(intent);
                         }
                    },
                    new Runnable() {
                        @Override
                        public void run() {
                            System.err.println("Failed to login...");
                        }
                    });
        }

        // Else, request a login
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View v) {
        //hide keyboard when button is clicked
        MainActivity.closeKeyboard(this);

        final String username = ((EditText)findViewById(R.id.loginUserInput)).getText().toString();
        final String password = ((EditText)findViewById(R.id.loginPassInput)).getText().toString();
        final TextView messageOutput = (TextView) LoginActivity.this.findViewById(R.id.messageOutput);
        messageOutput.setText(""); //whether or not there was an error in previous method call, clear it when login button clicked

        if (username.equals("") || password.equals("")) {
            messageOutput.setText("All fields must be set.");
        } else {
            final Button loginButton = (Button) findViewById(R.id.loginButton);
            loginButton.setClickable(false); //don't let user spam login button
            final ProgressBar loginSpinner = (ProgressBar) findViewById(R.id.loginSpinner);
            loginSpinner.setVisibility(View.VISIBLE);

            /*if this runs forever user can't click button again unless they restart the app,
            or they could switch to register view and then back to login (which basically loads a brand new instance of the view)
             */
            BeaconData.isValidLogin(username, password,
                    new Runnable() {
                        @Override
                        public void run() {
                            // Success!
                            BeaconData.registerLogin(LoginActivity.this, username, password);
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
                            // Failure!

                            messageOutput.setText("Incorrect username and or password.");
                            loginSpinner.setVisibility(View.GONE);
                            loginButton.setClickable(true);
                        }
                    });

        }
    }

    public void goToRegisterPage(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}