package com.tylerscave.safetravels;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * COPYRIGHT (C) 2017 TylersCave. All Rights Reserved.
 * The LocationService Class is used for all location related operations.
 * Its main function is accessing the user's last known location
 * @author Tyler Jones
 */
public class LocationService extends Service {
    // Global variables
    private SafeTravels safeTravels;
    private LocationManager locationManager = null;
    private Location updatedLocation;
    private Location networkLocation;
    private Location gpsLocation;
    private Location passiveLocation;
    private static final int LOCATION_INTERVAL = 0;
    private static final int LOCATION_DISTANCE = 0;
    private final String LOCATION_ACTION = "com.tylerscave.safetravels.action.LOCATION";
    private LocationListener[] locationListeners;


//##################################### Android Lifecycle ##############################################################
    /**
     * onCreate is used to initialize the location listeners and location manager
     */
    @Override
    public void onCreate() {
        safeTravels = SafeTravels.getInstance();
        locationListeners = new LocationListener[] {
                new LocationListener(LocationManager.GPS_PROVIDER),
                new LocationListener(LocationManager.NETWORK_PROVIDER),
                new LocationListener(LocationManager.PASSIVE_PROVIDER)
        };
        initLocationManager();
    }

    /**
     * onDestroy removes all location listeners and stops the LocationService
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            for (int i = 0; i < locationListeners.length; i++) {
                try {
                    locationManager.removeUpdates(locationListeners[i]);
                } catch (Exception ex) {
                    Log.i("LocationService", "fail to remove location listeners, ignore", ex);
                }
            }
        }
        stopSelf();
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
     * all providers and gets the last known location from each provider and stores the most recent location
     * @param intent
     * @param flags
     * @param startId
     * @return START_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // PASSIVE_PROVIDER
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[2]);
            passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (java.lang.SecurityException ex) {
            Log.i("LocationService", "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("LocationService", "passive provider does not exist " + ex.getMessage());
        }

        // NETWORK_PROVIDER
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[1]);
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (java.lang.SecurityException ex) {
            Log.i("LocationService", "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("LocationService", "network provider does not exist, " + ex.getMessage());
        }

        // GPS_PROVIDER
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    locationListeners[0]);
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (java.lang.SecurityException ex) {
            Log.i("LocationService", "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("LocationService", "gps provider does not exist " + ex.getMessage());
        }

        // Set most recent location and return
        this.setMostRecentLocation(updatedLocation, networkLocation, gpsLocation, passiveLocation);
        return START_STICKY;
    }

    /**
     * setMostRecentLocation is a helper method to set most recent last known location
     * @param locations
     */
    private void setMostRecentLocation(Location ... locations) {
        // Create arrays for the locations and their timestamps
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
        Long latest = Collections.max(timeStamps);
        for (Location location : myLocations) {
            if(location.getTime() == latest) {
                safeTravels.setLocation(location);
            }
        }
    }

    /**
     * locationEnabled() is used to determine whether the phones location services
     * are enabled or not
     * @return true if enabled, otherwise return false
     */
    protected boolean locationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * showLocationAlert() is used to alert the user that their location services are off.
     * It also gives the user the option to Enable Location
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
     * Private inner class to listen for location updates and broadcast them
     */
    private class LocationListener implements android.location.LocationListener {
        public LocationListener(String provider) {
            updatedLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location) {
            updatedLocation.set(location);
            Intent alarmIntent = new Intent();
            alarmIntent.setAction(LOCATION_ACTION);
            alarmIntent.putExtra("current_location", location);
            sendBroadcast(alarmIntent);
        }
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    @Override
    public IBinder onBind(Intent arg0) { return null; }
}