package com.ivanbenke.navbar;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by ivanb on 24.8.2017..
 */

public class ImageRetainingFragment extends Fragment {
    private Bitmap selectedImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setImage(Bitmap selectedImage) {
        this.selectedImage = selectedImage;
    }

    public Bitmap getImage() {
        return this.selectedImage;
    }
}
