package com.capstone.while1.beaconandroidstudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View v) {
        EditText userInput = (EditText) findViewById(R.id.loginUserInput);
        EditText passInput = (EditText) findViewById(R.id.loginPassInput);
        if (userInput == null || passInput == null) {
            Log.d("BeaconAndroidStudio", "userInput or passInput is null");
        } else {
            if (userInput.getText().toString().equals("user") && passInput.getText().toString().equals("pass")) {
                MainActivity.isLoggedIn = true;
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                TextView messageOutput = (TextView) findViewById(R.id.messageOutput);
                messageOutput.setText("Incorrect user/pass. Hint: user :: pass");
            }
        }
    }

    public void goToRegisterPage(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

    }
}