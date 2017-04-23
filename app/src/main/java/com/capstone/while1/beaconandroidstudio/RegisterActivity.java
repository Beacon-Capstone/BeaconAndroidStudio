package com.capstone.while1.beaconandroidstudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
        EditText userInput = (EditText) findViewById(R.id.usernameInput);
        EditText emailInput = (EditText) findViewById(R.id.emailInput);
        EditText passInput = (EditText) findViewById(R.id.passwordInput);
        EditText verifyPassInput = (EditText) findViewById(R.id.verifyPasswordInput);
        TextView messageOutput = (TextView) findViewById(R.id.messageOutput);

        String username = userInput.getText().toString();
        String password = passInput.getText().toString();
        String email = emailInput.getText().toString();
        String verifyPassword = verifyPassInput.getText().toString();

        if (! password.equals(verifyPassword)) {
            messageOutput.setText("Registration failed: password fields don't match.");
            return;
        }
        else {
            // Verify passed

        }
    }

    public void goToLoginPage(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}