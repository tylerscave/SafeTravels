package com.tylerscave.safetravels;

import android.app.AlarmManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * COPYRIGHT (C) 2017 TylersCave. All Rights Reserved.
 * StartActivity is the entry point for SafeTravels.
 * This is where the user enters contact information, duration between SMS messages, and starts the service
 * @author Tyler Jones
 */
public class StartActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    // Global variables
    private static SafeTravels safeTravels;
    private String contactNumber;
    private long defaultInterval;
    private LinearLayout mainView;
    private SMS sms;
    private LocationService locationService;

    // TextField in GUI
    private AutoCompleteTextView textView;
    private ArrayAdapter<String> adapter;

    // Radio Buttons
    private RadioGroup groupOne;
    private RadioGroup groupTwo;
    private boolean checking;
    private int selectedRadio;

    // HashMap for all needed contact info
    private Map<String, ArrayList<String>> contacts = new HashMap();

    // ArrayLists to store contact attributes
    private static ArrayList<String> numbers = new ArrayList<>();
    private static ArrayList<String> names = new ArrayList<>();


//##################################### Android Lifecycle ##############################################################
    /**
     * onCreate is used to initialize everything needed for this activity including user permissions
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mainView = (LinearLayout) findViewById(R.id.mainView);
        safeTravels = SafeTravels.getInstance();

        // Add icon to action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        // Check permissions and use all associated objects and services
        if (safeTravels.runtimePermission(StartActivity.this)) {
            sms = new SMS(this);
            // set the phone owners first name
            safeTravels.setOwnerName(sms.getPhoneOwner());
            // Get contacts and values in their perspective array lists
            contactNumber = "";
            contacts = sms.getContacts();
            numbers.addAll(contacts.get("numbers"));
            names.addAll(contacts.get("names"));

            // Initialize the locationService service and start it
            locationService = new LocationService();
            if (!locationService.locationEnabled(this)) {
                locationService.showLocationAlert(this);
            }
            safeTravels.startLocationService();
        }

        // Initialize AutoCompleteTextView values to contact names
        textView = (AutoCompleteTextView) findViewById(R.id.contactField);
        textView.setThreshold(1);
        //Create adapter for AutoCompleteTextView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        //Set adapter to AutoCompleteTextView
        textView.setAdapter(adapter);
        textView.setOnItemSelectedListener(this);
        textView.setOnItemClickListener(this);
        // Add all contact names to the adapter
        adapter.addAll(contacts.get("names"));

        // Set up and initialize Radio Button groups
        defaultInterval = Constants.THIRTY_MINUTES;
        selectedRadio = R.id.thirty_min;
        checking = true;
        groupOne = (RadioGroup) findViewById(R.id.group_one);
        groupTwo = (RadioGroup) findViewById(R.id.group_two);
        setRadioGroupListeners();

        // Initialize start button that will begin updates
        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(startClicked(textView));
    }

    /**
     * onResume is used to start/re-start the LocationService
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (safeTravels.runtimePermission(StartActivity.this)) {
            safeTravels.startLocationService();
        }
    }


//####################################### Listeners for TextView Adaptor ###############################################
    /**
     * onItemClick() is used to when a contact is selected from the list.
     * When this happens the user is presented with an alert showing contact name and number
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowID) {
        // Get index for selected name
        int i = names.indexOf("" + adapterView.getItemAtPosition(position));

        // If name exist in names get the mobile number
        if (i >= 0) {
            contactNumber = numbers.get(i);
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            // Show contact name and number in an alert
            Toast.makeText(getBaseContext(), "Name: " + adapterView.getItemAtPosition(position) + " " + "\nNumber:" + contactNumber,
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Remaining Methods needed for AdapterView
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long rowID) {}
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}


//######################################## Radio Button Listeners ######################################################
    /**
     * setRadioGroupListeners() is used to manage which radio button of two radio groups
     * has been selected
     */
    private void setRadioGroupListeners() {
        // Check what is selected in first group
        groupOne.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1 && checking) {
                    checking = false;
                    groupTwo.clearCheck();
                    selectedRadio = checkedId;
                }
                checking = true;
            }
        });
        // Check what is selected in the second group
        groupTwo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1 && checking) {
                    checking = false;
                    groupOne.clearCheck();
                    selectedRadio = checkedId;
                }
                checking = true;
            }
        });
    }


//########################################## Button Listeners ##########################################################
    /**
     * startClicked() is the listener for the start button. In a click event the information entered
     * for the contacts and time interval are captured in order to send an SMS message containing a
     * Google Maps URL to the selected contact. This button also starts Broadcasts to continue
     * sending messages at the selected time interval
     * @param contactField, the field containing the contact name
     * @return the listener
     */
    private OnClickListener startClicked(final AutoCompleteTextView contactField) {
        return new OnClickListener() {
            public void onClick(View v) {
                // Capture contact name from the contact field and most recent location
                final String contactName = contactField.getText().toString();
                final String number = contactNumber;

                // Store the selected interval value
                long interval = defaultInterval;
                if (selectedRadio == R.id.five_min) {
                    interval = Constants.FIVE_MINUTES;
                } else if (selectedRadio == R.id.ten_min) {
                    interval = Constants.TEN_MINUTES;
                } else if (selectedRadio == R.id.twenty_min) {
                    interval = Constants.TWENTY_MINUTES;
                } else if (selectedRadio == R.id.thirty_min) {
                    interval = Constants.THIRTY_MINUTES;
                } else if (selectedRadio == R.id.one_hour) {
                    interval = Constants.ONE_HOUR;
                } else if (selectedRadio == R.id.two_hour) {
                    interval = Constants.TWO_HOURS;
                }

                // If contact or location is invalid, notify user with snackBar notification
                if (number.length() == 0) {
                    Snackbar snackbar = Snackbar.make(mainView, "There was a problem with your contact\nPlease try again.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Cancel any active updates
                                    AlarmManager alarmManager = safeTravels.getAlarmManager();
                                    if (alarmManager != null) {
                                        alarmManager.cancel(safeTravels.getPendingLocationAlarmIntent());
                                        alarmManager.cancel(safeTravels.getPendingSmsAlarmIntent());
                                    }
                                    // Return to the main activity page
                                    startActivity(new Intent(getBaseContext(), StartActivity.class));
                                }
                            });
                    snackbar.setActionTextColor(Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                } else { // Send the update via text message
                    // Pass captured values to SafeTravels
                    safeTravels.setContactNumber(number);
                    safeTravels.setContactName(contactName);

                    // Set alarm to start the LocationService. A SMS alarm will be set from here
                    safeTravels.setLocationServiceBroadcastAlarm(interval);

                    // Switch to the running tracker page
                    startActivity(new Intent(StartActivity.this, RunningActivity.class));
                }
            }
        };
    }


//###################################### Runtime Permissions ###########################################################
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_MULTIPLE_PERMISSIONS:
                // Start-over to get permissions
                startActivity(new Intent(this, SplashActivity.class));
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}