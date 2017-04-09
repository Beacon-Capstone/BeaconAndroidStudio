package com.capstone.while1.beaconandroidstudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void register(View v) {
        EditText userInput = (EditText) findViewById(R.id.usernameInput);
        EditText emailInput = (EditText) findViewById(R.id.emailInput);
        EditText passInput = (EditText) findViewById(R.id.passwordInput);
        EditText verifyPassInput = (EditText) findViewById(R.id.verifyPasswordInput);
        TextView messageOutput = (TextView) findViewById(R.id.messageOutput);
        if (verifyPassInput.getText().toString().equals(passInput.getText().toString())) {
            messageOutput.setText("Registration Successful\nusername: " + userInput.getText() +
                    "\nemail: " + emailInput.getText());
            Log.d("BeaconAndroidStudio", "Registration Successful\nusername: " + userInput.getText() +
                "\nemail: " + emailInput.getText());
        } else {
            messageOutput.setText("Registration failed: password fields don't match.");
        }
    }

    public void goToLoginPage(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
