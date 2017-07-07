package com.tylerscave.safetravels;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import java.util.Calendar;

/**
 * COPYRIGHT (C) 2017 Tyler Jones. All Rights Reserved.
 * The SafeTravels class is responsible for storing and accessing variables that are needed
 * across the application. This class also maintains the broadcast alarm for sms updates
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
}
