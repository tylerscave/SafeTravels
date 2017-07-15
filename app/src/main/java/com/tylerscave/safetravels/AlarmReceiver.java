package com.tylerscave.safetravels;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

/**
 * COPYRIGHT (C) 2017 TylersCave. All Rights Reserved.
 * The AlarmReceiver class is used as a broadcast receiver to receive alarms at specified
 * intervals for sending SMS messages and for receiving location updates from the LocationService
 * @author Tyler Jones
 */
public class AlarmReceiver extends BroadcastReceiver {
    // Declare intent filters
    private final String SMS_ACTION = "com.tylerscave.safetravels.action.SMS";
    private final String LOCATION_ACTION = "com.tylerscave.safetravels.action.LOCATION";

    @Override
    public void onReceive(final Context context, Intent intent) {
        // Declare needed variables
        final SafeTravels safeTravels = SafeTravels.getInstance();
        SMS sms = new SMS(context);
        String action = intent.getAction();

        // Update SafeTravels when a location update was received
        if (LOCATION_ACTION.equals(action)) {
            Location lastLocation = (Location) intent.getExtras().get("current_location");
            safeTravels.setLocation(lastLocation);
        }

        // Send SMS with current location when SMS alarm fires
        if (SMS_ACTION.equals(action)) {
            // Get current contact number and last known location
            String contactNumber = safeTravels.getContactNumber();
            Location oldLocation = safeTravels.getLocation();
            Location newLocation = safeTravels.getLocation();

            // Start the LocationService and get most recent updated location
            context.startService(new Intent(context, LocationService.class));
            if (oldLocation.getTime() >= newLocation.getTime()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                newLocation = safeTravels.getLocation();
            }

            // Store the new location, send the SMS, and stop the LocationService
            safeTravels.setLocation(newLocation);
            sms.sendSMS(contactNumber, newLocation);
            context.stopService(new Intent(context, LocationService.class));
        }
    }
}
