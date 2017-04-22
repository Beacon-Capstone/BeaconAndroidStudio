package com.capstone.while1.beaconandroidstudio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.capstone.while1.beaconandroidstudio.beacondata.*;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // If BeaconData is not initialized, initiate it!
        if (! BeaconData.isInitialized()) {
            BeaconData.initiate(this);
        }

        // If already logged in, go to the map page immediately
        if (BeaconData.loginDataExists(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        // Else, request a login
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View v) {
        EditText userInput = (EditText) findViewById(R.id.loginUserInput);
        EditText passInput = (EditText) findViewById(R.id.loginPassInput);

        if (userInput == null || passInput == null) {
            Log.d("BeaconAndroidStudio", "userInput or passInput is null");
        } else {
            String username = userInput.getText().toString();
            String password = passInput.getText().toString();

            // Login doesn't exist yet, because this view loaded
            // Send off the username and password
            BeaconData.isValidLogin(username, password,
                    () -> {
                        // Success!
                        BeaconData.registerLogin(this, username, password);
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    },
                    () -> {
                        // Failure!
                        TextView messageOutput = (TextView) findViewById(R.id.messageOutput);
                        messageOutput.setText("Incorrect user/pass. Hint: user :: pass");
                    });
        }
    }

    public void goToRegisterPage(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}