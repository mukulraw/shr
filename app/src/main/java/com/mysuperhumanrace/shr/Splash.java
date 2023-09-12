package com.mysuperhumanrace.shr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {

    Timer timer;
    String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        requestStoragePermission();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            appSignature();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }

    }

    private void appSignature() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures = packageInfo.signatures;
            Signature appSignature = signatures[0];
            byte[] signatureBytes = appSignature.toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(signatureBytes);

            StringBuilder fingerprint = new StringBuilder();
            for (byte b : digest) {
                fingerprint.append(String.format("%02X:", b));
            }

//            Log.d("AppSignature", "SHA-1 Fingerprint: " + fingerprint.toString());

            String embeddedSignature = "AC:4B:F8:0F:8B:F8:08:14:93:A9:B8:E6:FA:58:F4:3A:2B:48:0E:14:";
//            Log.d("AppSignature", "Embedded Signature: " + embeddedSignature);

            if (fingerprint.toString().equals(embeddedSignature)) {
                Log.d("permmmkfjbdfkbndj", "1");
                appPackageName();
            } else {
                Toast.makeText(this, "Signature not match", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    private void appPackageName() {
        PackageManager packageManager = getPackageManager();
        String installerPackageName = packageManager.getInstallerPackageName(getPackageName());
        System.out.println(installerPackageName);
        if ("com.android.vending".equals(installerPackageName)) {
            RootDetectionActivity();
        } else {
            Toast.makeText(this, "This app was not installed from the Google Play Store. Proceed with caution.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void RootDetectionActivity() {
        if (checkForRootAccess()) {
            // Root is detected, show a toast and then close the app
            Toast.makeText(this, "Root access detected. This app cannot run on rooted devices.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            startApp();
        }
    }
    private boolean checkForRootAccess() {
        StringBuilder detectionResults = new StringBuilder();

        // Check for root management apps
        if (checkRootManagementApps()) {
            detectionResults.append("Root management apps found\n");
        }

        // Check for potentially dangerous apps
//        if (checkPotentiallyDangerousApps()) {
//            detectionResults.append("Potentially dangerous apps found\n");
//        }

        // Check for root cloaking apps
        if (checkRootCloakingApps()) {
            detectionResults.append("Root cloaking apps found\n");
        }

        // Check for test keys
        if (checkTestKeys()) {
            detectionResults.append("Test keys detected\n");
        }

        // Check for dangerous system properties
//        if (checkForDangerousProps()) {
//            detectionResults.append("Dangerous system properties found\n");
//        }

        // Check for BusyBox binary
        if (checkForBusyBoxBinary()) {
            return checkForBinary("busybox");//function is available below
        }

        // Check for su binary
        if (checkForSuBinary()) {
            detectionResults.append("su binary found\n");
        }

        // Check for su binary existence
        if (checkSuExists()) {
            detectionResults.append("su binary exists\n");
        }

        // Display the root detection results
        if (detectionResults.length() > 0) {
            Toast.makeText(this, "Device may be rooted:\n" + detectionResults.toString(), Toast.LENGTH_LONG).show();
            return true;

        } else {
            Toast.makeText(this, "Device is not rooted", Toast.LENGTH_LONG).show();
            return false;

        }
    }


    private boolean checkRootCloakingApps() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA);
        String[] rootCloakingAppPackageNames  = {   "com.devadvance.rootcloak",
                "com.devadvance.rootcloakplus",
                "de.robv.android.xposed.installer",
                "com.saurik.substrate",
                "com.zachspong.temprootremovejb",
                "com.amphoras.hidemyroot",
                "com.amphoras.hidemyrootadfree",
                "com.formyhm.hiderootPremium",
                "com.formyhm.hideroot"
        };
        for (ApplicationInfo packageInfo : packages) {
            for (String rootCloakingAppPackageName : rootCloakingAppPackageNames) {
                if (packageInfo.packageName.equals(rootCloakingAppPackageName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkForSuBinary() {
        return checkForBinary("su"); // function is available below

    }
    private boolean checkForBusyBoxBinary() {
        return checkForBinary("busybox");
    }

    private boolean checkTestKeys() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private boolean checkRootManagementApps() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.chainfire.supersu")) {
                return true;
            }
        }

        return false;
    }
    /**
     * @param filename - check for this existence of this
     * file("su","busybox")
     * @return true if exists
     */
    private boolean checkForBinary(String filename) {
        for (String path : binaryPaths) {
            File f = new File(path, filename);
            boolean fileExists = f.exists();
            if (fileExists) {
                return true;
            }
        }
        return false;
    }
    private String[] binaryPaths= {
            "/data/local/",
            "/data/local/bin/",
            "/data/local/xbin/",
            "/sbin/",
            "/su/bin/",
            "/system/bin/",
            "/system/bin/.ext/",
            "/system/bin/failsafe/",
            "/system/sd/xbin/",
            "/system/usr/we-need-root/",
            "/system/xbin/",
            "/system/app/Superuser.apk",
            "/cache",
            "/data",
            "/dev"
    };

    /**
     * A variation on the checking for SU, this attempts a 'which su'
     * different file system check for the su binary
     * @return true if su exists
     */
    private boolean checkSuExists() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]
                    {"/system /xbin/which", "su"});
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line = in.readLine();
            process.destroy();
            return line != null;
        } catch (Exception e) {
            if (process != null) {
                process.destroy();
            }
            return false;
        }
    }
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Log.d("permmm", "1");


                appSignature();

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                ) {

                    Log.d("permmm", "3");

                    Toast.makeText(getApplicationContext(), "Permissions are required for this app", Toast.LENGTH_SHORT).show();
                    finish();

                } else {

                    Log.d("permmm", "4");
                    Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
            }
        }

    }
    private void startApp() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {


                Intent i = new Intent(Splash.this , MainActivity.class);
                startActivity(i);
                finish();

            }
        } , 1200);

    }

}
