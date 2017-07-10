package com.tylerscave.safetravels;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * COPYRIGHT (C) 2017 Tyler Jones. All Rights Reserved.
 * RunningTrackerActivity is is presented to the user when updates have begun.
 * @author Tyler Jones
 */
public class RunningTrackerActivity extends AppCompatActivity {

    // Global Variables
    private SafeTravels safeTravels;
    private ImageView imageView;
    private AlarmManager alarmManager;
    private PendingIntent pendingAlarmIntent;
    private AnimationDrawable catDrivingAnimation;
    private Button stopButton;
    private Button startAgainButton;

//##################################### Android Lifecycle ##############################################################

    /**
     * onCreate is used to initialize everything needed for this activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_tracker);

        // Add icon to action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        // Add the cat driving animation to the image view
        imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setBackgroundResource(R.drawable.cat_driving);
        catDrivingAnimation = (AnimationDrawable) imageView.getBackground();

        // Set variables
        safeTravels = SafeTravels.getInstance();
        alarmManager = safeTravels.getAlarmManager();
        pendingAlarmIntent = safeTravels.getPendingAlarmIntent();

        // Initialize buttons
        stopButton = (Button)findViewById(R.id.stopButton);
        stopButton.setVisibility(View.VISIBLE);
        startAgainButton = (Button)findViewById(R.id.startAgainButton);
        startAgainButton.setVisibility(View.GONE);
        // Button Click listeners
        stopButton.setOnClickListener(stopClicked());
        startAgainButton.setOnClickListener(startAgainClicked());

        // Update text to show who is being updated
        String updatingString = safeTravels.getContactName().toUpperCase() + " will be updated!";
        ((TextView)findViewById (R.id.running_one)).setText (updatingString);

        // Stop location updates until alarm fires
        stopService(new Intent(this, LocationService.class));
    }

    /**
     * onResume is used to start the cat driving animation in the imageview
     */
    @Override
    protected void onResume() {
        super.onResume();
        catDrivingAnimation.start();
    }

    /**
     * onDestroy is used to ensure the LocationService and the Alarm has been stopped when the activity is killed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopIt();
    }

//########################################## Button Listeners ##########################################################

    /**
     * stopClicked cancels any pending location updates and stops the LocationService if running
     * when stop is pressed the UI changes and user is presented with new options
     * @return
     */
    private OnClickListener stopClicked() {
        return new OnClickListener() {
            public void onClick(View v) {
                // Cancel the updates
                Toast.makeText(getApplicationContext(), "******* Updates Canceled ******", Toast.LENGTH_LONG).show();
                stopIt();

                // Update text and image
                catDrivingAnimation.stop();
                imageView.setBackgroundResource(R.drawable.updates_completed);
                ((TextView)findViewById (R.id.running_one)).setText (R.string.stopped_text);

                // Change from stop button to start again button
                stopButton.setVisibility(View.GONE);
                startAgainButton.setVisibility(View.VISIBLE);
            }
        };
    }

    /**
     * startAgainClicked brings the user back to the main activity to start updating a new contact
     * @return
     */
    private OnClickListener startAgainClicked() {
        return new OnClickListener() {
            public void onClick(View v) {
                // Return to the main activity page
                Intent myIntent = new Intent(RunningTrackerActivity.this, MainActivity.class);
                startActivity(myIntent);
            }
        };
    }

//######################################## Helper methods ##############################################################

    /**
     * stopIt() stops any scheduled alarms and the location service
     */
    private void stopIt() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingAlarmIntent);
        }
        stopService(new Intent(RunningTrackerActivity.this, LocationService.class));
    }
}
