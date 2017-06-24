package com.tylerscave.safetravels;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * COPYRIGHT (C) 2017 Tyler Jones. All Rights Reserved.
 * The SplashActivity displays the SafeTravels logo on startup
 * @author Tyler Jones
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity( new Intent(this, MainActivity.class));
        finish();
    }
}
