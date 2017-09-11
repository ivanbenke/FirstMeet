package com.ivanbenke.navbar;

/**
 * Created by ivanb on 27.6.2017..
 */


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
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
                    //send data
                    /*mNdefMessage = new NdefMessage(
                            new NdefRecord[]{
                                    createNewTextRecord(first_name, Locale.ENGLISH, true),
                                    createNewTextRecord(last_name, Locale.ENGLISH, true),
                                    createNewTextRecord(phone_number, Locale.ENGLISH, true)
                            }
                    );
                    mNfcAdapter.setNdefPushMessage(mNdefMessage, getActivity(), getActivity());*/
                    /*String message = first_name + last_name + phone_number;
                    Log.d("Orientation", message);
                    NdefRecord ndefRecord = NdefRecord.createMime("text/plain", message.getBytes());
                    NdefMessage ndefMessage = new NdefMessage(ndefRecord);
                    Log.d("Orientation", "" + ndefMessage);
                    mNfcAdapter.setNdefPushMessage(ndefMessage, getActivity());*/
                    /*mNfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
                        @Override
                        public NdefMessage createNdefMessage(NfcEvent event) {
                            String message = first_name + last_name + phone_number;
                            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] { createMime("application/vnd.com.ivanbenke.navbar", message.getBytes()) } );
                            return ndefMessage;
                        }
                    }, getActivity());*/ //valja

                    jsonMyInfo = new JSONObject();
                    try {
                        jsonMyInfo.put("firstname", first_name);
                        jsonMyInfo.put("lastname", last_name);
                        jsonMyInfo.put("phonenumber", phone_number);
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "JSONException" , Toast.LENGTH_SHORT).show();
                    }

                    try {
                        jsonFileName = "myContactInfo.json";
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
                            // Do something else on failure
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Exception" , Toast.LENGTH_SHORT).show();
                    }

                    mNfcAdapter.setBeamPushUrisCallback(new NfcAdapter.CreateBeamUrisCallback() {
                        @Override
                        public Uri[] createBeamUris(NfcEvent event) {
                            imageFileName = "ProfilePhoto.jpg";
                            picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            imageFile = new File(picDir, imageFileName);
                            imageFile.setReadable(true, false);
                            return new Uri[]{Uri.fromFile(imageFile), Uri.fromFile(jsonFile)};
                        }
                    }, getActivity());
                }
            }
        });

        return rootView;
    }
}
