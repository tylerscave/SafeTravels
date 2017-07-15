package com.tylerscave.safetravels;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * COPYRIGHT (C) 2017 TylersCave. All Rights Reserved.
 * The SMS class handles all sms activities for SafeTravels
 * This class uses the user's contact list for accessing mobile numbers and sends the SMS message
 * @author Tyler Jones
 */
public class SMS {
    private Context context;

    /**
     * Constructor for SMS sets context
     * @param context
     */
    public SMS(Context context) {
        this.context = context;
    }

    /**
     * getContacts() is used to get contact information from the device
     * this is used to provide user with autocomplete contact search.
     * @return contacts, hashmap of names and mobile numbers
     */
    protected Map<String, ArrayList<String>> getContacts() {
        // Containers for contact information
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> numbers = new ArrayList<String>();
        Map<String, ArrayList<String>> contacts = new HashMap();

        // Needed to query contacts
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        // If there are any contacts stored in this device
        if (cursor.getCount() > 0) {
            // Iterate over contacts and get name and id for each
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // Set cursor for contacts with phone numbers only
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{id}, null);

                    // Iterate over contact info
                    while (phoneCursor.moveToNext()) {
                        // Get only contacts with mobile numbers
                        String mobileNumber;
                        int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                            mobileNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            // Add contact info to array lists
                            numbers.add(mobileNumber.toString());
                            names.add(name.toString());
                        }
                    }
                    phoneCursor.close();
                }
            }
            cursor.close();
        }
        // Put the arrayLists into the hashmap and return
        contacts.put("names", names);
        contacts.put("numbers", numbers);
        return contacts;
    }

    /**
     * sendSMS() is used to send a SMS message with the users current location
     */
    protected void sendSMS(String phoneNumber, Location currentLocation) {
        // Initialize the SMS manager and get timestamp and phone owner
        SafeTravels safeTravels = SafeTravels.getInstance();
        SmsManager smsManager = SmsManager.getDefault();
        String timeStamp = getTimeStamp();
        String ownerName = safeTravels.getOwnerName();

        // Prepare the text message
        StringBuffer smsBody = new StringBuffer();
        smsBody.append(ownerName + "'s location from SafeTravels!\n\n");
        smsBody.append("https://maps.google.com/maps?q=");
        smsBody.append("My+Location+at+"+timeStamp+"@");
        smsBody.append(currentLocation.getLatitude());
        smsBody.append(",");
        smsBody.append(currentLocation.getLongitude());
        smsBody.append("&ll=");
        smsBody.append(currentLocation.getLatitude());
        smsBody.append(",");
        smsBody.append(currentLocation.getLongitude());
        smsBody.append("&z=13");

        // Send the text to the desired user
        smsManager.sendTextMessage(phoneNumber, null, smsBody.toString(), null, null);
    }

    /**
     * getPhoneOwner() is used to query the phone for the owners first name
     * @return the phone owners first name
     */
    protected String getPhoneOwner() {
        final String[] OWNER = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(ContactsContract.Profile.CONTENT_URI, OWNER, null, null, null);
        cursor.moveToFirst();
        String ownerName = cursor.getString(0);
        String nameArray[] = ownerName.split(" ", 2);
        cursor.close();
        return nameArray[0];
    }

    /**
     * getTimeStamp() is a helper method used to get a simple string representing the
     * current time on a 12 hour clock.
     * @return timeStamp, the current time
     */
    private String getTimeStamp() {
        long timeInMillis = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mma");
        String timeStamp = sdf.format(cal.getTime());
        return timeStamp;
    }
}
