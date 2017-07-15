package com.tylerscave.safetravels;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * COPYRIGHT (C) 2017 TylersCave. All Rights Reserved.
 * The SafeTravels class is responsible for storing and accessing variables that are needed
 * across the application. This class also maintains the broadcast alarm for sms updates and
 * holds much of the runtime permissions methods so they can be called from multiple activities
 * @author Tyler Jones
 */
public class SafeTravels extends Application {
    // Global variables
    private static SafeTravels safeTravels;
    private AlarmManager alarmManager;
    private PendingIntent pendingAlarmIntent;
    private Location location;
    private String contactName;
    private String contactNumber;
    private String phoneOwner;
    private final String SMS_ACTION = "com.tylerscave.safetravels.action.SMS";
    private final int REQUEST_CODE_MULTIPLE_PERMISSIONS = 666;

    /**
     * Constructor for SafeTravels
     * Initialize all needed variables
     */
    @Override
    public void onCreate() {
        super.onCreate();
        safeTravels = this;
        // Initialize variables needed for broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.setAction(SMS_ACTION);
        pendingAlarmIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    /**
     * getInstance() is used to get a singleton instance of SafeTravels
     * @return SafeTravels
     */
    protected static SafeTravels getInstance() {
        if (safeTravels == null) {
            safeTravels = new SafeTravels();
        }
        return safeTravels;
    }

    /**
     * setBroadcastAlarm is called from main activity to set the SMS alarm
     */
    protected void setBroadcastAlarm(int interval) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pendingAlarmIntent);
    }


//############## Getters and Setters for variables needed across the SafeTravels application #####################
    protected AlarmManager getAlarmManager() {
        return alarmManager;
    }

    protected PendingIntent getPendingAlarmIntent() {
        return pendingAlarmIntent;
    }

    protected Location getLocation() {
        return location;
    }
    protected void setLocation(Location location) {
        this.location = location;
    }

    protected String getContactNumber() {
        return contactNumber;
    }
    protected void setContactNumber(String number) {
        this.contactNumber = number;
    }

    protected String getContactName() {
        return contactName;
    }
    protected void setContactName(String name) {
        this.contactName = name;
    }

    protected String getOwnerName() {
        return phoneOwner;
    }
    protected void setOwnerName(String name) {
        this.phoneOwner = name;
    }


//################################################# Runtime Permissions ################################################
    /**
     * runtimePermissions is used to check if runtime permissions are needed (Marshmallow 6.0+).
     * @return true if permission is granted or if before Marshmallow. return false if any permissions are needed
     */
    protected boolean runtimePermission(final AppCompatActivity activityContext) {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissionsNeeded = new ArrayList<>();
            final List<String> permissionsList = new ArrayList<>();
            if (!addPermission(activityContext, permissionsList, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                    !addPermission(activityContext, permissionsList, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                permissionsNeeded.add("access location");
            if (!addPermission(activityContext, permissionsList, android.Manifest.permission.READ_CONTACTS))
                permissionsNeeded.add("read contacts");
            if (!addPermission(activityContext, permissionsList, android.Manifest.permission.SEND_SMS))
                permissionsNeeded.add("send SMS messages");

            if (permissionsList.size() > 0) {
                if (permissionsNeeded.size() > 0) {
                    // rationale
                    String message = "To use SafeTravels, you MUST allow the app to " + permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++) {
                        message = message + ", " + permissionsNeeded.get(i);
                    }
                    permissionsDialog(activityContext, message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        ActivityCompat.requestPermissions(activityContext,
                                                permissionsList.toArray(new String[permissionsList.size()]),
                                                REQUEST_CODE_MULTIPLE_PERMISSIONS);
                                    }
                                }
                            });
                    return false;
                }
                ActivityCompat.requestPermissions(activityContext,
                        permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_MULTIPLE_PERMISSIONS);
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * addPermissions is a helper method to add permissions to the permission list
     * @param activityContext
     * @param permissionsList
     * @param permission
     * @return true if permission has been granted
     */
    private boolean addPermission(final AppCompatActivity activityContext, List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(activityContext, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activityContext, permission))
                return false;
        }
        return true;
    }

    /**
     * permissionsDialog notifies the user of the permissions needed to run SafeTravels
     * @param activityContext
     * @param message
     * @param okListener
     */
    private void permissionsDialog(final AppCompatActivity activityContext, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activityContext)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     * runAppDialog is a dialog to remind the user that they must allow the permissions
     * @param activityContext
     */
    protected void runAppDialog (final AppCompatActivity activityContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setMessage("You MUST allow ALL requested permissions to use SafeTravels");
        builder.setCancelable(true);
        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        runtimePermission(activityContext);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
