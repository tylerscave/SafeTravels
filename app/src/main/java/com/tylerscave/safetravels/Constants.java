package com.tylerscave.safetravels;

/**
 * COPYRIGHT (C) 2017 TylersCave. All Rights Reserved.
 * The Constants class holds all of the Constant values for SafeTravels
 * @author Tyler Jones
 */

public class Constants {

    // Time in milliseconds
    static final int FIVE_SECONDS = 1000 * 5;
    static final int TEN_SECONDS = 1000 * 10;
    static final int THIRTY_SECONDS = 1000 * 30;
    static final int ONE_MINUTE = 1000 * 60;
    static final int TWO_MINUTES = 1000 * 60 * 2;
    static final int FIVE_MINUTES = 1000 * 60 * 5;
    static final int TEN_MINUTES = 1000 * 60 * 10;
    static final int TWENTY_MINUTES = 1000 * 60 * 20;
    static final int THIRTY_MINUTES = 1000 * 60 * 30;
    static final int ONE_HOUR = 1000 * 60 * 60;
    static final int TWO_HOURS = 1000 * 60 * 60 * 2;

    // Intent Filters for broadcast alarm
    static final String SMS_ACTION = "com.tylerscave.safetravels.action.SMS";
    static final String LOCATION_ACTION = "com.tylerscave.safetravels.action.LOCATION";
    static final String LOCATION_SERVICE_ACTION = "com.tylerscave.safetravels.action.LOCATION_SERVICE";

    // ID's
    static final int REQUEST_CODE_MULTIPLE_PERMISSIONS = 666;
    static final int NOTIFICATION_ID = 111;

    // Location parameters
    static final int LOCATION_INTERVAL = 0;
    static final int LOCATION_DISTANCE = 0;
}
