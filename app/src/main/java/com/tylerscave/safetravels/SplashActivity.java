package com.tylerscave.safetravels;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

/**
 * COPYRIGHT (C) 2017 TylersCave. All Rights Reserved.
 * The SplashActivity displays the SafeTravels logo on startup.
 * It also handles the initial permissions request for M+
 * @author Tyler Jones
 */
public class SplashActivity extends AppCompatActivity {
    private static SafeTravels safeTravels;
    private final int REQUEST_CODE_MULTIPLE_PERMISSIONS = 666;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        safeTravels = SafeTravels.getInstance();

        // Check for permission and start main activity if already granted otherwise wait for permission
        if (safeTravels.runtimePermission(SplashActivity.this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }


//######################################## Runtime Permissions #########################################################
    /**
     * onRequestPermissionResult is the callback for requestPermissions(). If permission is granted move on to the
     * MainActivity. If permission is not granted, notify user that it is needed and ask again.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> permissionsMap = new HashMap<>();
                // fill with all possible permissions initially
                permissionsMap.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                permissionsMap.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                permissionsMap.put(android.Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                permissionsMap.put(android.Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
                // Replace with the results when you get them
                for (int i = 0; i < permissions.length; i++)
                    permissionsMap.put(permissions[i], grantResults[i]);

                // Check for all needed permissions
                if (permissionsMap.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        permissionsMap.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        permissionsMap.get(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                        permissionsMap.get(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    // If all permissions have been granted move to the main activity
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    // Notify the user that they must allow the permissions to use the app
                    safeTravels.runAppDialog(SplashActivity.this);
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
