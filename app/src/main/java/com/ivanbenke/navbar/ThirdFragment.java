package com.ivanbenke.navbar;

/**
 * Created by ivanb on 27.6.2017..
 */

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ThirdFragment extends Fragment {

    public ThirdFragment() {

    }

    String mParentPath, imageFileName, firstname, lastname, phonenumber, content, jsonFileName, latitude, longitude;
    File jsonFile, imageFile, appDir, contactImage, contactJson;
    JSONObject jObj;
    private static final int REQUEST_LOCATION_PERMISSION = 10;
    LocationListener mLocationListener;
    LocationManager mLocationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.third_fragment, container, false);

        mLocationListener = new SimpleLocationListener();
        mLocationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        if (TextUtils.equals(action, Intent.ACTION_VIEW)) {
            Uri beamUri = intent.getData();
            Log.d("parentpath", "" + beamUri);
            if (TextUtils.equals(beamUri.getScheme(), "file")) {
                mParentPath = handleFileUri(beamUri);
                Log.d("parentpath", mParentPath);
            } else if (TextUtils.equals(beamUri.getScheme(), "content")) {
                mParentPath = handleContentUri(beamUri);
                Log.d("parentpath", mParentPath);
            }

            imageFile = new File(mParentPath + "/ProfilePhoto.jpg");
            jsonFile = new File(mParentPath + "/myContactInfo.json");

            parseJSON(jsonFile);

            imageFileName = firstname+lastname+"Photo.jpg";
            jsonFileName = firstname+lastname+"ContactInfo.json";
            appDir = new File(Environment.getExternalStorageDirectory() + "/FirstMeet");
            contactImage = new File(appDir, imageFileName);
            contactJson = new File(appDir, jsonFileName);
            try {
                copyFile(imageFile, contactImage);
                copyFile(jsonFile, contactJson);
                imageFile.delete();
                jsonFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }

            parseJSON(contactJson);

            if(!hasPermission()) {
                requestPermission();
            } else {
                startTracking();
            }

            ContactInfoDbHelper.getInstance(getActivity()).insertInfo(firstname, lastname, phonenumber, latitude, longitude);
        }
    }

    private void startTracking() {
        Log.d("Tracking", "Tracking started.");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String locationProvider = mLocationManager.getBestProvider(criteria, true);
        long minTime = 1000;
        float minDistance = 10;
        try {
            mLocationManager.requestLocationUpdates(locationProvider, minTime, minDistance, mLocationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateLocation(Location location){
        latitude = "" + location.getLatitude();
        longitude = "" + location.getLongitude();
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
            Log.d("directory", firstname+lastname+phonenumber);
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
        // Get the path part of the URI
        String fileName = beamUri.getPath();
        // Create a File object for this filename
        File copiedFile = new File(fileName);
        // Get a string containing the file's parent directory
        return copiedFile.getParent();
    }

    public String handleContentUri(Uri beamUri) {
        // Position of the filename in the query Cursor
        int filenameIndex;
        // File object for the filename
        File copiedFile;
        // The filename stored in MediaStore
        String fileName;
        // Test the authority of the URI
        if (!TextUtils.equals(beamUri.getAuthority(), MediaStore.AUTHORITY)) {
            /*
             * Handle content URIs for other content providers
             */
            // For a MediaStore content URI
            return null;
        } else {
            // Get the column that contains the file name
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor pathCursor = getActivity().getContentResolver().query(beamUri, projection, null, null, null);
            // Check for a valid cursor
            if (pathCursor != null && pathCursor.moveToFirst()) {
                // Get the column index in the Cursor
                filenameIndex = pathCursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                // Get the full file name including path
                fileName = pathCursor.getString(filenameIndex);
                // Create a File object for the filename
                copiedFile = new File(fileName);
                pathCursor.close();
                // Return the parent directory of the file
                return copiedFile.getParent();
            } else {
                pathCursor.close();
                // The query didn't work; return null
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
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
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

    private class SimpleLocationListener implements LocationListener{
        @Override public void onLocationChanged(Location location) { updateLocation(location); }
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(String provider) { }
        @Override public void onProviderDisabled(String provider) {}
    }
}
