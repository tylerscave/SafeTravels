package com.tylerscave.safetravels;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

/**
 * COPYRIGHT (C) 2017 TylersCave. All Rights Reserved.
 * The AlarmReceiver class is used as a broadcast receiver to receive alarms at specified
 * intervals for starting the LocationService, sending SMS messages, and for receiving
 * location updates from the LocationService
 * @author Tyler Jones
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        // Declare needed variables
        final SafeTravels safeTravels = SafeTravels.getInstance();
        final SMS sms = new SMS(context);
        final String action = intent.getAction();

        switch (action) {
            // Update SafeTravels when a location update was received
            case Constants.LOCATION_ACTION:
                Location lastLocation = (Location) intent.getExtras().get("current_location");
                safeTravels.setLocation(lastLocation);
                break;
            // Start the LocationService and set alarm to send SMS (allowing some time to capture new location)
            case Constants.LOCATION_SERVICE_ACTION:
                if (safeTravels.isLocationServiceRunning(LocationService.class)) {
                    safeTravels.setSmsBroadcastAlarm(Constants.TEN_SECONDS);
                } else {
                    safeTravels.startLocationService();
                    safeTravels.setSmsBroadcastAlarm(Constants.THIRTY_SECONDS);
                }
                safeTravels.stopCountdownTimer();
                break;
            // Send SMS with current location when SMS alarm fires
            case Constants.SMS_ACTION:
                // Get current contact number and last known location
                final String contactNumber = safeTravels.getContactNumber();
                final Location location = safeTravels.getLocation();
                // Send the SMS and stop the LocationService after sent
                if (location != null) {
                    sms.sendSMS(contactNumber, location);
                    safeTravels.stopLocationService();
                } else { // wait to capture location and try again
                    safeTravels.setSmsBroadcastAlarm(Constants.FIVE_SECONDS);
                }
                break;
            // Default is to do nothing.
            default:
                break;
        }
    }
}