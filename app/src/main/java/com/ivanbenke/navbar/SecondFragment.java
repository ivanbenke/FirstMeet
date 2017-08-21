package com.ivanbenke.navbar;

/**
 * Created by ivanb on 27.6.2017..
 */

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import static android.app.Activity.RESULT_OK;

public class SecondFragment extends Fragment implements View.OnClickListener {

    public SecondFragment() {

    }

    /*private static final int CAMERA_REQUEST = 1888;*/
    private Uri uriFilePath;

    String tag;

    ImageView ivPicture;
    ImageButton bTakePicture;
    TextView tvFirstName, tvLastName, tvPhoneNumber;
    ImageButton bEdit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.second_fragment, container, false);

        View.OnClickListener mOpenCamera = new View.OnClickListener() {
            public void onClick(View v) {
                /*Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);*/
                PackageManager packageManager = getActivity().getPackageManager();
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    File mainDirectory = new File(Environment.getExternalStorageDirectory(), "MyFolder/tmp");
                    if (!mainDirectory.exists())
                        mainDirectory.mkdirs();

                    Calendar calendar = Calendar.getInstance();

                    uriFilePath = Uri.fromFile(new File(mainDirectory, "IMG_" + calendar.getTimeInMillis()));
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriFilePath);
                    startActivityForResult(intent, 1);
                }
            }
        };

        if (savedInstanceState != null) {
            if (uriFilePath == null && savedInstanceState.getString("uri_file_path") != null) {
                uriFilePath = Uri.parse(savedInstanceState.getString("uri_file_path"));
            }
        }

        ivPicture = (ImageView) rootView.findViewById(R.id.ivPicture);
        bTakePicture = (ImageButton) rootView.findViewById(R.id.bTakePicture);
        tvFirstName = (TextView) rootView.findViewById(R.id.tvFirstName);
        tvLastName = (TextView) rootView.findViewById(R.id.tvLastName);
        tvPhoneNumber = (TextView) rootView.findViewById(R.id.tvPhoneNumber);
        bEdit = (ImageButton) rootView.findViewById(R.id.bEdit);

        List<String> myInfo = MyInfoDbHelper.getInstance(getActivity()).getMyInfo();
        if(!myInfo.isEmpty()) {
            tvFirstName.setText(myInfo.get(0));
            tvLastName.setText(myInfo.get(1));
            tvPhoneNumber.setText(myInfo.get(2));
        }

        bTakePicture.setOnClickListener(mOpenCamera);
        bEdit.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        Fragment fragment = null;
        Class fragmentClass;
        fragmentClass = FourthFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (uriFilePath != null)
            outState.putString("uri_file_path", uriFilePath.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ivPicture.setImageBitmap(photo);
        }*/
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                String filePath = uriFilePath.getPath(); // Here is path of your captured image, so you can create bitmap from it, etc.

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
                Log.v(tag, "filePath variable: " + filePath + ", uriFilePath: " + uriFilePath);
                ivPicture.setImageBitmap(bitmap);
            }
        }
    }
}
