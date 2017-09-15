package com.ivanbenke.navbar;

/**
 * Created by ivanb on 27.6.2017..
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class ThirdFragment extends Fragment {

    public ThirdFragment() {

    }

    ListView lvContacts;
    ContactAdapter mContactAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.third_fragment, container, false);

        lvContacts = (ListView) rootView.findViewById(R.id.lvContacts);
        mContactAdapter = new ContactAdapter(this.loadContacts());
        lvContacts.setAdapter(mContactAdapter);
        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parent.getItemAtPosition(position);
                TextView tvContactID = (TextView) view.findViewById(R.id.tvContactID);
                String contactID = tvContactID.getText().toString();
                Fragment fragment = null;
                Class fragmentClass;
                fragmentClass = FifthFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //FifthFragment fragment = new FifthFragment();
                Bundle args = new Bundle();
                args.putString("contactID", contactID);
                fragment.setArguments(args);

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).addToBackStack(null).commit();
            }
        });

        return rootView;
    }

    private ArrayList<Contact> loadContacts() {
        return ContactInfoDbHelper.getInstance(getActivity()).getAllContacts();
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(R.string.fContacts);
    }
}
