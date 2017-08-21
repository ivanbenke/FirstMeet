package com.ivanbenke.navbar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class FourthFragment extends Fragment implements View.OnClickListener {

    public FourthFragment() {

    }

    EditText etFirstName, etLastName, etPhoneNumber;
    Button bSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fourth_fragment, container, false);

        etFirstName = (EditText) rootView.findViewById(R.id.etFirstName);
        etLastName = (EditText) rootView.findViewById(R.id.etLastName);
        etPhoneNumber = (EditText) rootView.findViewById(R.id.etPhoneNumber);
        bSave = (Button) rootView.findViewById(R.id.bSave);

        bSave.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        String first_name = etFirstName.getText().toString();
        String last_name = etLastName.getText().toString();
        String phone_number = etPhoneNumber.getText().toString();
        MyInfoDbHelper.getInstance(getActivity()).insertInfo(first_name, last_name, phone_number);

        hideKeyboard(getActivity());

        Fragment fragment = null;
        Class fragmentClass;
        fragmentClass = SecondFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
