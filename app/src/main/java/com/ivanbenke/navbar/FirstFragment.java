package com.ivanbenke.navbar;

/**
 * Created by ivanb on 27.6.2017..
 */


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.List;

public class FirstFragment extends Fragment {

    public FirstFragment() {

    }

    private NfcAdapter mNfcAdapter;
    String first_name, last_name, phone_number, imageFileName, jsonFileName;
    Button bShare;
    File jsonFile, appDir, picDir, imageFile;
    Writer output;
    JSONObject jsonMyInfo;
    String mParentPath, imageContactFileName, firstname, lastname, phonenumber, content, jsonContactFileName, latitude, longitude;
    File jsonContactFile, imageContactFile, appContactDir, contactImage, contactJson;
    JSONObject jObj;
    private static final int REQUEST_LOCATION_PERMISSION = 10;
    LocationManager mLocationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.first_fragment, container, false);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(getContext());

        List<String> myInfo = MyInfoDbHelper.getInstance(getActivity()).getMyInfo();
        if (!myInfo.isEmpty()) {
            first_name = myInfo.get(0);
            last_name = myInfo.get(1);
            phone_number = myInfo.get(2);
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("LOCATION", Context.MODE_PRIVATE);
        latitude = prefs.getString("latitude", "");
        longitude = prefs.getString("longitude", "");
        Log.d("latlongonCreateTracking", latitude + " lat, " + longitude + " lon");

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        bShare = (Button) rootView.findViewById(R.id.bShare);
        bShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNfcAdapter.isEnabled()) {
                    AlertDialog.Builder alertbox = new AlertDialog.Builder(getContext());
                    alertbox.setTitle("NFC Permission");
                    alertbox.setMessage("We need your permission for sending data via NFC");
                    alertbox.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }
                        }
                    });
                    alertbox.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertbox.show();
                } else {
                    if (hasPermission()) {
                        startTracking();
                    }

                    jsonMyInfo = new JSONObject();
                    try {
                        jsonMyInfo.put("firstname", first_name);
                        jsonMyInfo.put("lastname", last_name);
                        jsonMyInfo.put("phonenumber", phone_number);
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "JSONException" , Toast.LENGTH_SHORT).show();
                    }

                    try {
                        jsonFileName = "myContactInfo.txt";
                        appDir = new File(Environment.getExternalStorageDirectory() + "/FirstMeet");
                        boolean success = true;
                        if (!appDir.exists()) {
                            success = appDir.mkdir();
                        }
                        if (success) {
                            jsonFile = new File(appDir, jsonFileName);
                            output = new BufferedWriter(new FileWriter(jsonFile));
                            output.write(jsonMyInfo.toString());
                            jsonFile.setReadable(true, false);
                            output.close();
                        } else {

                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Your data hasn't been entered or you haven't granted read/write permissions" , Toast.LENGTH_SHORT).show();
                    }

                    mNfcAdapter.setBeamPushUrisCallback(new NfcAdapter.CreateBeamUrisCallback() {
                        @Override
                        public Uri[] createBeamUris(NfcEvent event) {
                            imageFileName = "ProfilePhoto.jpg";
                            picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            imageFile = new File(picDir, imageFileName);
                            imageFile.setReadable(true, false);
                            return new Uri[]{Uri.fromFile(jsonFile), Uri.fromFile(imageFile)};
                        }
                    }, getActivity());
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!hasPermission()) {
            requestPermission();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(R.string.fShare);
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        if (TextUtils.equals(action, Intent.ACTION_VIEW)) {
            Uri beamUri = intent.getData();
            if (beamUri != null) {
                if (beamUri.getScheme().equals("file")) {
                    mParentPath = handleFileUri(beamUri);
                } else if (beamUri.getScheme().equals("content")) {
                    if (beamUri.toString().startsWith("content://0@media/")) {
                        //do something
                    } else {
                        mParentPath = handleContentUri(beamUri);
                    }
                }

                imageContactFile = new File(mParentPath + "/ProfilePhoto.jpg");
                jsonContactFile = new File(mParentPath + "/myContactInfo.txt");

                if (jsonContactFile.exists()) {
                    parseJSON(jsonContactFile);

                    imageContactFileName = firstname + lastname + "Photo.jpg";
                    jsonContactFileName = firstname + lastname + "ContactInfo.txt";
                    appContactDir = new File(Environment.getExternalStorageDirectory() + "/FirstMeet");
                    contactImage = new File(appContactDir, imageContactFileName);
                    contactJson = new File(appContactDir, jsonContactFileName);
                    try {
                        copyFile(imageContactFile, contactImage);
                        copyFile(jsonContactFile, contactJson);
                        imageContactFile.delete();
                        jsonContactFile.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    parseJSON(contactJson);
                }

                File myInfoFile = new File(appContactDir, first_name + last_name + "ContactInfo.txt");
                File myContactFile = new File(appContactDir, "myContactInfo.txt");
                if (myInfoFile.exists()) {
                    try {
                        copyFile(myInfoFile, myContactFile);
                        myInfoFile.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                SharedPreferences prefs = getActivity().getSharedPreferences("LOCATION", Context.MODE_PRIVATE);
                latitude = prefs.getString("latitude", "");
                longitude = prefs.getString("longitude", "");
                Log.d("latlongonResumeTracking", latitude + " lat, " + longitude + " lon");
                if (latitude != null && longitude != null) {
                    Log.d("latlongInsideTracking", latitude + " lat, " + longitude + " lon");
                    ContactInfoDbHelper.getInstance(getActivity()).insertInfo(firstname, lastname, phonenumber, latitude, longitude);
                    prefs.edit().remove("latitude").apply();
                    prefs.edit().remove("longitude").apply();
                }
            }
        } else {
            Log.d("Error", "Error with receiving Android Beam");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stopTracking();
    }

    private void stopTracking() {
        Log.d("Tracking", "Tracking stopped.");
        mLocationManager.removeUpdates(mLocationListener);
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Enable Location")
                .setMessage("You need to enable location to use this app")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void startTracking() {
        if(!checkLocation())
            return;
        Log.d("Tracking", "Tracking started");
        Toast.makeText(getContext(), "Tracking started, please wait until location is updated", Toast.LENGTH_SHORT).show();
        String locationProvider = LocationManager.GPS_PROVIDER;
        long minTime = 1000;
        float minDistance = 10;
        try {
            mLocationManager.requestLocationUpdates(locationProvider, minTime, minDistance, mLocationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void parseJSON(File jsonFile) {
        content = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(jsonFile));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                content = sb.toString();
            } finally {
                br.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }

        try {
            jObj = new JSONObject(content);
            firstname = jObj.getString("firstname");
            lastname = jObj.getString("lastname");
            phonenumber = jObj.getString("phonenumber");
        } catch (JSONException e) {
            Toast.makeText(getContext(), "JSONException" , Toast.LENGTH_SHORT).show();
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

    public String handleFileUri(Uri beamUri) {
        String fileName = beamUri.getPath();
        File copiedFile = new File(fileName);
        return copiedFile.getParent();
    }

    public String handleContentUri(Uri beamUri) {
        int filenameIndex;
        File copiedFile;
        String fileName;
        if (!beamUri.getAuthority().equals(MediaStore.AUTHORITY)) {
            return null;
        } else {
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor pathCursor = getActivity().getContentResolver().query(beamUri, projection, null, null, null);
            if (pathCursor != null && pathCursor.moveToFirst()) {
                filenameIndex = pathCursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                fileName = pathCursor.getString(filenameIndex);
                copiedFile = new File(fileName);
                pathCursor.close();
                return copiedFile.getParent();
            } else {
                pathCursor.close();
                return null;
            }
        }
    }

    private boolean hasPermission(){
        int statusLocation = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if(statusLocation == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        Log.d("Permission","Permission granted. User pressed allow.");
                    }
                    else {
                        Log.d("Permission","Permission not granted. User pressed deny.");
                        askForPermission();
                    }
                }
                break;
        }
    }

    private void askForPermission() {
        boolean explainRead = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if(explainRead) {
            Log.d("Permission","Permission should be explained, - don't show again not clicked.");
            displayDialog();
        }
        else {
            Log.d("Permission","Permission not granted. User pressed deny and don't show again.");
        }
    }

    private void displayDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Location permission")
                .setMessage("We need your permission to store the location of your first meet")
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Permission", "User declined and won't be asked again.");
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Permission","Permission requested because of the explanation.");
                        requestPermission();
                        dialog.cancel();
                    }
                })
                .show();
    }

    LocationListener mLocationListener = new LocationListener () {
        @Override public void onLocationChanged(Location location) { updateLocation(location); }
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(String provider) { }
        @Override public void onProviderDisabled(String provider) {}
    };

    private void updateLocation(Location location){
        latitude = "" + location.getLatitude();
        longitude = "" + location.getLongitude();
        Toast.makeText(getContext(), "Location updated", Toast.LENGTH_SHORT).show();
        Log.d("latlongTracking", latitude + " lat, " + longitude + " lon");
        SharedPreferences prefs = getActivity().getSharedPreferences("LOCATION", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString("latitude", latitude);
        prefsEdit.putString("longitude", longitude);
        prefsEdit.apply();
    }
}
