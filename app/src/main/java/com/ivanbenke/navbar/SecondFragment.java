package com.ivanbenke.navbar;

/**
 * Created by ivanb on 27.6.2017..
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SecondFragment extends Fragment {

    public SecondFragment() {

    }

    private static final String FRAGMENT_NAME = "imageFragment";
    private ImageRetainingFragment imageRetainingFragment;
    Uri mCapturedImageURI, mImage, mImageUri;
    String mCurrentImagePath;
    private static final int REQUEST_CODE_PERMISSION = 10;
    ImageView ivPicture;
    ImageButton bTakePicture, bEdit;
    TextView tvFirstName, tvLastName, tvPhoneNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.second_fragment, container, false);

        ivPicture = (ImageView) rootView.findViewById(R.id.ivPicture);
        bTakePicture = (ImageButton) rootView.findViewById(R.id.bTakePicture);
        tvFirstName = (TextView) rootView.findViewById(R.id.tvFirstName);
        tvLastName = (TextView) rootView.findViewById(R.id.tvLastName);
        tvPhoneNumber = (TextView) rootView.findViewById(R.id.tvPhoneNumber);
        bEdit = (ImageButton) rootView.findViewById(R.id.bEdit);

        SharedPreferences prefs = getContext().getSharedPreferences("IMAGE", Context.MODE_PRIVATE);
        String mImageUriString = prefs.getString("ImageUri", "");
        mImageUri = Uri.parse(mImageUriString);
        mCurrentImagePath = prefs.getString("ImagePath", "");
        if (mImageUri != null && !mCurrentImagePath.isEmpty()) {
            try {
                ivPicture.setImageBitmap(getScaledBitmap(mImageUri));
            } catch (IOException e) {
                Toast.makeText(getContext(), "IOException" , Toast.LENGTH_SHORT).show();
            }
        }

        List<String> myInfo = MyInfoDbHelper.getInstance(getActivity()).getMyInfo();
        if(!myInfo.isEmpty()) {
            tvFirstName.setText(myInfo.get(0));
            tvLastName.setText(myInfo.get(1));
            tvPhoneNumber.setText(myInfo.get(2));
        }

        bTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasPermission()) {
                    requestPermission();
                } else {
                    openCamera();
                }
            }
        });

        bEdit.setOnClickListener(new View.OnClickListener() {
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
        });

        initializeImageRetainingFragment();
        tryLoadImage();

        return rootView;
    }

    private boolean hasPermission(){
        int statusRead = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int statusWrite = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int statusCamera = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        if(statusRead == PackageManager.PERMISSION_GRANTED && statusWrite == PackageManager.PERMISSION_GRANTED && statusCamera == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        requestPermissions(permissions, REQUEST_CODE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                        Log.d("Permission","Permission granted. User pressed allow.");
                        openCamera();
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
        boolean explainRead = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean explainWrite = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean explainCamera = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA);
        if(explainRead || explainWrite || explainCamera) {
            Log.d("Permission","Permission should be explained, - don't show again not clicked.");
            displayDialog();
        }
        else {
            Log.d("Permission","Permission not granted. User pressed deny and don't show again.");
        }
    }

    private void displayDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Storage and Camera permissions")
                .setMessage("We need your permission for taking and storing an image to external storage")
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

    private void openCamera() {
        ShowDialog(100, 1000);
    }

    private void initializeImageRetainingFragment() {
        // find the retained fragment on activity restarts
        FragmentManager fragmentManager = getFragmentManager();
        imageRetainingFragment = (ImageRetainingFragment) fragmentManager.findFragmentByTag(FRAGMENT_NAME);
        // create the fragment and bitmap the first time
        if (imageRetainingFragment == null) {
            imageRetainingFragment = new ImageRetainingFragment();
            fragmentManager.beginTransaction()
                    // Add a fragment to the activity state.
                    .add(imageRetainingFragment, FRAGMENT_NAME)
                    .commit();
        }
    }
    private void tryLoadImage() {
        if (imageRetainingFragment == null) {
            return;
        }

        Bitmap selectedImage = imageRetainingFragment.getImage();
        if (selectedImage == null) {
            return;
        }

        ivPicture.setImageBitmap(selectedImage);
    }

    void ShowDialog(final int req, final int choose) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("");
        builder.setTitle("Select Photo")
                .setNegativeButton("Take Photo", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                // Ensure that there's a camera activity to handle the intent
                                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    // Create the File where the photo should go
                                    File photoFile = null;
                                    try {
                                        photoFile = createImageFile();
                                    } catch (IOException ex) {
                                        // Error occurred while creating the File
                                    }
                                    // Continue only if the File was successfully created
                                    if (photoFile != null) {
                                        takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.ivanbenke.navbar.fileprovider", photoFile);
                                        mCapturedImageURI = photoURI;
                                        takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                                        startActivityForResult(takePictureIntent, req);
                                    }
                                }

                            }
                        })
                .setPositiveButton("Choose Existing", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                photoPickerIntent.setType("image/*");
                                startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), choose);
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private File createImageFile() throws IOException {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String imageFileName = "ProfilePhoto.jpg";
        File image = new File(storageDir, imageFileName);

        if (image.exists()){
            image.delete(); //DELETE existing file
            image = new File(storageDir, imageFileName);
        } else {
            image = new File(storageDir, imageFileName);
        }

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentImagePath = image.getAbsolutePath();
        Log.d("Orientation", mCurrentImagePath + " camera");
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // From camera
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            if (mCapturedImageURI != null) {
                mImage = mCapturedImageURI;
                try {
                    ivPicture.setImageBitmap(getScaledBitmap(mCapturedImageURI));
                } catch (IOException e) {
                    Toast.makeText(getContext(), "IOException" , Toast.LENGTH_SHORT).show();
                }
                System.out.println("On activity Result uri = " + mCapturedImageURI.toString());
            } else {
                Toast.makeText(getContext(), "Error getting Image", Toast.LENGTH_SHORT).show();
            }
        }

        //From gallery
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                System.out.println("Content Path : " + selectedImage.toString());
                if (selectedImage != null) {
                    mCurrentImagePath = getRealPathFromURI(selectedImage);
                    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String imageFileName = "ProfilePhoto.jpg";
                    File f = new File(storageDir, imageFileName);
                    if (f.exists()) {
                        try {
                            f.delete();
                            copyFile(new File(mCurrentImagePath), f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            copyFile(new File(mCurrentImagePath), f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        ivPicture.setImageBitmap(getScaledBitmap(selectedImage));
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "IOException" , Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error getting Image", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "No Photo Selected", Toast.LENGTH_SHORT).show();
            }
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

    private Bitmap getScaledBitmap (Uri uri) throws IOException{
        Bitmap source, rotated = null;
        mImageUri = uri;
        try {
            if (mImageUri != null && mCurrentImagePath != null) {
                SharedPreferences prefs = getContext().getSharedPreferences("IMAGE", Context.MODE_PRIVATE);
                SharedPreferences.Editor prefsEdit = prefs.edit();
                prefsEdit.putString("ImageUri", mImageUri.toString());
                prefsEdit.putString("ImagePath", mCurrentImagePath);
                prefsEdit.apply();
            }
            ContentResolver cr = getContext().getContentResolver();
            InputStream in = cr.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize=8;
            source = BitmapFactory.decodeStream(in, null, options);
            int rotation = getImageAngle(uri, mCurrentImagePath);
            Matrix matrix = new Matrix();
            matrix.preRotate(rotation);
            rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        } catch (FileNotFoundException e) {
            Toast.makeText(getContext(), "File not found" , Toast.LENGTH_SHORT).show();
        }

        if (imageRetainingFragment == null) {

        } else {
            imageRetainingFragment.setImage(rotated);
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

    public String getRealPathFromURI(Uri uri) {
        if (Build.VERSION.SDK_INT >= 19) {
            String id = uri.getLastPathSegment().split(":")[0];
            final String[] imageColumns = {MediaStore.Images.Media.DATA };
            final String imageOrderBy = null;
            Uri tempUri = getUri();
            Cursor imageCursor = getContext().getContentResolver().query(tempUri, imageColumns, MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);
            if (imageCursor.moveToFirst()) {
                return imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            } else {
                return null;
            }
        } else {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else
                return null;
        }
    }

    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }
}