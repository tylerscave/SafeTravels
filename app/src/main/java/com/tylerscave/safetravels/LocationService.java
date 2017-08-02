package com.tylerscave.safetravels;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * COPYRIGHT (C) 2017 TylersCave. All Rights Reserved.
 * The LocationService Class is used for all location related operations.
 * Its main function is accessing the user's last known location and starting location updates
 * @author Tyler Jones
 */
public class LocationService extends Service {

    // Define needed variables
    private SafeTravels safeTravels;
    private LocationManager locationManager = null;
    private Location updatedLocation;
    private LocationListener[] locationListeners;


//##################################### Android Lifecycle ##############################################################
    /**
     * onCreate is used to initialize the location listeners and location manager
     */
    @Override
    public void onCreate() {
        super.onCreate();
        safeTravels = SafeTravels.getInstance();
        locationListeners = new LocationListener[] {
                new LocationListener(LocationManager.GPS_PROVIDER),
                new LocationListener(LocationManager.NETWORK_PROVIDER),
                new LocationListener(LocationManager.PASSIVE_PROVIDER)
        };
        initLocationManager();
    }

    /**
     * onTaskRemoved calls stopIt() to remove all location listeners and stop the LocationService
     */
    @Override
    public void onTaskRemoved(Intent intent) {
        this.stopIt();
    }

    /**
     * onDestroy calls stopIt() to remove all location listeners and stop the LocationService
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopIt();
    }


//##################################### LocationService Methods ########################################################
    /**
     * initLocationManager initializes the location manager if not already initialized
     */
    private void initLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
    }

    /**
     * onStartCommand is called when the LocationService is started. The method starts location updates for
     * all providers and sets the best last known location. It also starts the service as a foreground
     * service with a notification.
     * @param intent
     * @param flags
     * @param startId
     * @return START_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // Create notification for foreground service and start the service
        Intent notificationIntent = new Intent(this, RunningActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingNotificationIntent)
                .build();
        startForeground(Constants.NOTIFICATION_ID, notification);

        // Set the best last location and start the location updates with a timer
        this.getProvidersLocations();
        safeTravels.startCountdownTimer(this);
        return START_STICKY;
    }


    /**
     * getProvidersLocations starts location updates for all providers and gets the last known location
     * from each provider and stores the best location primarily based on the most recent option, but also
     * considering location accuracy
     */
    private void getProvidersLocations() {
        // Initialize all location variables with last stored location
        final Location oldLocation = safeTravels.getLocation();
        Location networkLocation = oldLocation;
        Location gpsLocation = oldLocation;
        Location passiveLocation = oldLocation;
        final int MAX_TIME_DIFF = Constants.ONE_MINUTE;

        // Get last known location from each provider and start the location updates
        // PASSIVE_PROVIDER
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListeners[2]);
            passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (java.lang.SecurityException ex) {
            Log.i("LocationService", "fail to request passive provider location update ", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("LocationService", "passive provider does not exist " + ex.getMessage());
        }
        // NETWORK_PROVIDER
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListeners[1]);
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (java.lang.SecurityException ex) {
            Log.i("LocationService", "fail to request network provider location update ", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("LocationService", "network provider does not exist, " + ex.getMessage());
        }
        // GPS_PROVIDER
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, Constants.LOCATION_INTERVAL, Constants.LOCATION_DISTANCE, locationListeners[0]);
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (java.lang.SecurityException ex) {
            Log.i("LocationService", "fail to request gps provider location update", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("LocationService", "gps provider does not exist " + ex.getMessage());
        }

        // Get the most recent and most accurate last known locations
        Location lastLocation = safeTravels.getLocation();
        Location mostRecentLocation = this.getMostRecentLocation(networkLocation, gpsLocation, passiveLocation, lastLocation);
        Location mostAccurateLocation = this.getMostAccurateLocation(networkLocation, gpsLocation, passiveLocation, lastLocation);

        // If locations are valid, set the best location based on time
        if (mostRecentLocation != null && mostAccurateLocation != null) {
            if (mostRecentLocation.getTime() - mostAccurateLocation.getTime() < MAX_TIME_DIFF) {
                safeTravels.setLocation(mostAccurateLocation);
            } else {
                safeTravels.setLocation(mostRecentLocation);
            }
        }
    }

    /**
     * setMostRecentLocation is a helper method to get the most recent location
     * @param locations
     */
    private Location getMostRecentLocation(Location ... locations) {
        Location recentLocation = null;
        List<Location> myLocations = new ArrayList<>();
        List<Long> timeStamps = new ArrayList<>();

        // Add all valid locations and timestamps to arrays
        for (Location location : locations) {
            if (location != null) {
                myLocations.add(location);
                timeStamps.add(location.getTime());
            }
        }
        // Get the most recent location and set it
        if (timeStamps.size() > 0) {
            Long latest = Collections.max(timeStamps);
            for (Location location : myLocations) {
                if (location.getTime() == latest) {
                    recentLocation = location;
                }
            }
        }
        return recentLocation;
    }

    /**
     * setMostAccurateLocation is a helper method to get the most accurate location
     * @param locations
     */
    private Location getMostAccurateLocation (Location ... locations) {
        Location accurateLocation = null;
        List<Location> myLocations = new ArrayList<>();
        List<Float> accuracies = new ArrayList<>();

        // Add all valid locations and accuracies to arrays
        for (Location location : locations) {
            if (location != null) {
                myLocations.add(location);
                accuracies.add(location.getAccuracy());
            }
        }
        // Get the most accurate location and set it
        if (accuracies.size() > 0) {
            Float mostAccurate = Collections.min(accuracies);
            for (Location location : myLocations) {
                if (location.getAccuracy() == mostAccurate) {
                    accurateLocation = location;
                }
            }
        }
        return accurateLocation;
    }

    /**
     * locationEnabled() is used to determine whether the phones location services are enabled or not.
     * @return true if enabled, otherwise return false
     */
    protected boolean locationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * showLocationAlert() is used to alert the user that their location services are off.
     * It also gives the user the option to Enable Location.
     * @param activity
     */
    protected void showLocationAlert(Activity activity) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        paramDialogInterface.dismiss();
                        getApplicationContext().startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    /**
     * stopIt() removes all location listeners and stops the LocationService
     */
    private void stopIt() {
        if (locationManager != null) {
            for (int i = 0; i < locationListeners.length; i++) {
                try {
                    locationManager.removeUpdates(locationListeners[i]);
                } catch (Exception ex) {
                    Log.i("LocationService", "fail to remove location listeners. ", ex);
                }
            }
        }
        stopSelf();
    }

    /**
     * Must override onBind in any service, but we are not binding our service so we simply return null
     * @param intent
     * @return null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}


//########################################## Location Listener Class ###################################################
    /**
     * Private inner class to listen for location updates and broadcast them
     */
    private class LocationListener implements android.location.LocationListener {
        public LocationListener(String provider) {
            updatedLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location) {
            updatedLocation.set(location);
            // Send broadcast with location intent to update SafeTravels with updated location
            Intent locationAlarmIntent = new Intent();
            locationAlarmIntent.setAction(Constants.LOCATION_ACTION);
            locationAlarmIntent.putExtra("current_location", location);
            sendBroadcast(locationAlarmIntent);
        }
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}