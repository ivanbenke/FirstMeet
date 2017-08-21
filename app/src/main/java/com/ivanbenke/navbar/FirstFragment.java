package com.ivanbenke.navbar;

/**
 * Created by ivanb on 27.6.2017..
 */


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FirstFragment extends Fragment implements View.OnClickListener {

    public FirstFragment() {

    }

    Button bShare;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.first_fragment, container, false);

        bShare = (Button) rootView.findViewById(R.id.bShare);

        bShare.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick (View v) {
        //NFC
    }
}
