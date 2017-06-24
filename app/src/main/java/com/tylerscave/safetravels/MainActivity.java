package com.tylerscave.safetravels;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
 * COPYRIGHT (C) 2017 Tyler Jones. All Rights Reserved.
 * MainActivity is the entry point for SafeTravels.
 * This is where the user enters contact information, duration between SMS messages, and starts the service
 *
 * @author Tyler Jones
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    // Global variables
    private static SafeTravels safeTravels;
    private String contactNumber = "";
    private int defaultInterval = 1000 * 60 * 30; // Thirty minutes
    private LinearLayout mainView;
    private SMS sms;
    private LocationService locationService;

    // TextField in GUI
    private AutoCompleteTextView textView = null;
    private ArrayAdapter<String> adapter;

    // Radio Buttons
    private RadioGroup groupOne;
    private RadioGroup groupTwo;
    private boolean checking = true;
    private int selectedRadio = R.id.thirty_min;

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
        setContentView(R.layout.activity_main);
        mainView = (LinearLayout) findViewById(R.id.mainView);
        safeTravels = SafeTravels.getInstance();

        // Initialize the sms (text message) object to handle all sms services
        sms = new SMS(this);
        // Get contacts and values in their perspective array lists
        contacts = sms.getContacts();
        numbers.addAll(contacts.get("numbers"));
        names.addAll(contacts.get("names"));

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
        groupOne = (RadioGroup) findViewById(R.id.group_one);
        groupTwo = (RadioGroup) findViewById(R.id.group_two);
        setRadioGroupListeners();

        // Initialize start button that will begin updates
        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(startClicked(textView));

        // Initialize the locationService service
        locationService = new LocationService();
        if (!locationService.locationEnabled(this)) {
            locationService.showLocationAlert(this);
        }
        // Start the location service
        if (runtimePermission()) {
            startService(new Intent(this, LocationService.class));
        }
    }

    /**
     * onResume is used to start/re-start the LocationService
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (runtimePermission()) {
            startService(new Intent(this, LocationService.class));
        }
    }

    /**
     * onStop stops the location service when the activity is no longer actively being used
     */
    @Override
    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, LocationService.class));
    }

    /**
     * onDestroy is used to ensure the LocationService has been stopped when the activity is killed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, LocationService.class));
    }

//################################################# Runtime Permissions ################################################

    /**
     * runtimePermissions checks runtime location permissions and requests permission if not granted
     * @return true if permission is granted
     */
    private boolean runtimePermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},100);
            return false;
        }
        return true;
    }

    /**
     * onRequestPermissionResult is the callback for requestPermissions(). If permission is granted do nothing and
     * start using location. If permission is not granted, notify user that it is needed and ask again.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                // start getting and using location
            } else {
                Toast.makeText(this, "You must enable locations to use this app", Toast.LENGTH_SHORT).show();
                runtimePermission();
            }
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


//####################################### Radio Button Listeners ######################################################

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

//####################################### Button Listeners ##########################################################

    /**
     * startClicked() is the listener for the start button. In a click event the information entered
     * for the contacts and time interval are captured and used with the users last known location
     * in order to send an SMS message containing a Google Maps URL to the selected contact. This
     * button also starts Broadcasts to continue sending messages at the selected time interval
     *
     * @param contactField, the field containing the contact name
     * @return the listener
     */
    private OnClickListener startClicked(final AutoCompleteTextView contactField) {
        return new OnClickListener() {
            public void onClick(View v) {
                // Capture contact name from the contact field and most recent location
                final String contactName = contactField.getText().toString();
                final String number = contactNumber;
                final Location location = safeTravels.getLocation();

                // Store the selected interval value
                int interval = defaultInterval;
                if (selectedRadio == R.id.one_min) {
                    interval = 1000 * 60 * 1; // One minute
                } else if (selectedRadio == R.id.five_min) {
                    interval = 1000 * 60 * 5; // Five minutes
                } else if (selectedRadio == R.id.ten_min) {
                    interval = 1000 * 60 * 10; // Ten minutes
                } else if (selectedRadio == R.id.thirty_min) {
                    interval = 1000 * 60 * 30; // Thirty minutes
                } else if (selectedRadio == R.id.one_hour) {
                    interval = 1000 * 60 * 60; // One hour
                } else if (selectedRadio == R.id.six_hour) {
                    interval = 1000 * 60 * 60 * 6; // Six hours
                }

                // If contact or location is invalid, notify user with snackBar notification
                if (number.length() == 0 || location == null) {
                    if (number.length() == 0) {
                        Toast.makeText(getBaseContext(), "Please enter a contact", Toast.LENGTH_LONG).show();
                    }
                    if (location == null) {
                        Toast.makeText(getBaseContext(), "Your Location is Unavailable\nPlease try again", Toast.LENGTH_LONG).show();
                    }
                    Snackbar snackbar = Snackbar.make(mainView, "There was a problem!!!", Snackbar.LENGTH_INDEFINITE)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Cancel any active updates
                                    AlarmManager alarmManager = safeTravels.getAlarmManager();
                                    if (alarmManager != null) {
                                        alarmManager.cancel(safeTravels.getPendingAlarmIntent());
                                    }
                                    // Return to the main activity page
                                    Intent errorIntent = new Intent(getBaseContext(), MainActivity.class);
                                    startActivity(errorIntent);
                                }
                            });
                    snackbar.setActionTextColor(Color.RED);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();

                // Else send the update via text message
                } else {
                    // Pass captured values to SafeTravels
                    safeTravels.setContactNumber(number);
                    safeTravels.setContactName(contactName);

                    // Send an initial SMS and set broadcast alarm for updates.
                    safeTravels.setBroadcastAlarm(interval);

                    // Switch to the running tracker page
                    Intent myIntent = new Intent(MainActivity.this, RunningTrackerActivity.class);
                    startActivity(myIntent);
                }
            }
        };
    }
}
