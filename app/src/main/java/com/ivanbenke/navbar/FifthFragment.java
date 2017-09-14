package com.ivanbenke.navbar;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.media.ExifInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * Created by ivanb on 13.9.2017..
 */

public class FifthFragment extends Fragment {

    public FifthFragment() {

    }

    String contactID, firstName, lastName, phoneNumber, latitude, longitude;
    TextView tvContactInfoFirstName, tvContactInfoLastName, tvContactInfoPhoneNumber;
    Button bContactInfoLocation;
    ImageView ivContactInfoPicture;
    File appDir, contactPicture;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fifth_fragment, container, false);

        if (this.getArguments() != null) {
            contactID = this.getArguments().getString("contactID", "");

            ContactInfo contactInfo = ContactInfoDbHelper.getInstance(getActivity()).getContact(contactID);
            firstName = contactInfo.getFirstName();
            lastName = contactInfo.getLastName();
            phoneNumber = contactInfo.getPhoneNumber();
            latitude = contactInfo.getLatitude();
            longitude = contactInfo.getLongitude();
        }

        ivContactInfoPicture = (ImageView) rootView.findViewById(R.id.ivContactInfoPicture);
        tvContactInfoFirstName = (TextView) rootView.findViewById(R.id.tvContactInfoFirstName);
        tvContactInfoLastName = (TextView) rootView.findViewById(R.id.tvContactInfoLastName);
        tvContactInfoPhoneNumber = (TextView) rootView.findViewById(R.id.tvContactInfoPhoneNumber);
        bContactInfoLocation = (Button) rootView.findViewById(R.id.bContactInfoLocation);

        appDir = new File(Environment.getExternalStorageDirectory() + "/FirstMeet");
        contactPicture = new File(appDir, firstName + lastName + "Photo.jpg");

        if (contactPicture.exists()) {
            try {
                ivContactInfoPicture.setImageBitmap(getScaledBitmap(Uri.fromFile(contactPicture), contactPicture.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        tvContactInfoFirstName.setText(firstName);
        tvContactInfoLastName.setText(lastName);
        tvContactInfoPhoneNumber.setText(phoneNumber);

        bContactInfoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "http://maps.google.com/maps?daddr=" + latitude + "," + longitude + " (" + firstName + " " + lastName + ")";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try
                {
                    startActivity(intent);
                }
                catch(ActivityNotFoundException ex)
                {
                    try
                    {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(unrestrictedIntent);
                    }
                    catch(ActivityNotFoundException innerEx)
                    {
                        Toast.makeText(getContext(), "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return rootView;
    }

    private Bitmap getScaledBitmap (Uri uri, String imagePath) throws IOException {
        Bitmap source, rotated = null;
        try {
            ContentResolver cr = getContext().getContentResolver();
            InputStream in = cr.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize=8;
            source = BitmapFactory.decodeStream(in, null, options);
            int rotation = getImageAngle(uri, imagePath);
            Matrix matrix = new Matrix();
            matrix.preRotate(rotation);
            rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(), "File not found" , Toast.LENGTH_SHORT).show();
        }
        return rotated;
    }

    private int getImageAngle(Uri uri, String imagePath) {
        int orientation;

        if (imagePath == null) {
            return 0;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                orientation = getOrientation(uri);
            } else {
                orientation = getOrientationLegacy(imagePath);
            }

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
            return 0;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private int getOrientation(Uri uri) {
        InputStream in = null;
        ExifInterface exif = null;

        try {
            in = getContext().getContentResolver().openInputStream(uri);
            exif = new ExifInterface(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        return getExifAttributeInt(exif);
    }

    private int getOrientationLegacy(String imagePath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getExifAttributeInt(exif);
    }

    private int getExifAttributeInt(ExifInterface exif) {
        if (exif != null) {
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } else {
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }
}
