package com.capstone.while1.beaconandroidstudio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.capstone.while1.beaconandroidstudio.beacondata.BeaconConsumer;
import com.capstone.while1.beaconandroidstudio.beacondata.BeaconData;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // If BeaconData is not initialized, initiate it!
        if (! BeaconData.isQueueInitialized()) {
            BeaconData.initiateQueue(this);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void register(View v) {
        Log.d("Start Register", (new Long(System.currentTimeMillis()).toString()));
        EditText userInput = (EditText) findViewById(R.id.usernameInput);
        EditText emailInput = (EditText) findViewById(R.id.emailInput);
        EditText passInput = (EditText) findViewById(R.id.passwordInput);
        EditText verifyPassInput = (EditText) findViewById(R.id.verifyPasswordInput);
        final TextView messageOutput = (TextView) findViewById(R.id.messageOutput);

        final String username = userInput.getText().toString();
        final String password = passInput.getText().toString();
        final String email = emailInput.getText().toString();
        final String verifyPassword = verifyPassInput.getText().toString();

        final RegisterActivity currentContext = this;

        if (! password.equals(verifyPassword)) {
            messageOutput.setText("Registration failed: password fields don't match.");
            return;
        }
        else {
            // Verify passed

            // Make sure the username isn't taken
            BeaconData.isValidUsername(username, new BeaconConsumer<Boolean>() {
                @Override
                public void accept(Boolean isTaken) {
                    if (! isTaken) {
                        // The username is not taken!!!
                        // Create the account!
                        BeaconData.createUser(username, email, password,
                                new Runnable() {
                                    // Successfully created the user!
                                    @Override
                                    public void run() {
                                        BeaconData.registerLogin(currentContext, username, password);
                                        Intent intent = new Intent(currentContext, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        currentContext.startActivity(intent);
                                        RegisterActivity.this.finish();
                                    }
                                },
                                new BeaconConsumer<String>() {
                                    @Override
                                    public void accept(String obj) {
                                        // Failed to create the user...
                                        messageOutput.setText("Registration failed.");
                                    }
                                });
                    }
                    else {
                        // Username has been taken
                        messageOutput.setText("Registration failed: Username has been taken.");
                    }
                }
            });

            // Create the user!

        }
    }
}