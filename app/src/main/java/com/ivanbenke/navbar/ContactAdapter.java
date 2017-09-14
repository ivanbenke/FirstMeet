package com.ivanbenke.navbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

/**
 * Created by ivanb on 13.9.2017..
 */

public class ContactAdapter extends BaseAdapter {

        private ArrayList<Contact> mContacts;

        public ContactAdapter(ArrayList<Contact> contacts) {
            mContacts = contacts;
        }
        @Override
        public int getCount() { return this.mContacts.size(); }
        @Override
        public Object getItem(int position) { return this.mContacts.get(position); }
        @Override
        public long getItemId(int position) { return position; }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder contactViewHolder;
            if(convertView == null){
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.item_contact, parent, false);
                contactViewHolder = new ViewHolder(convertView);
                convertView.setTag(contactViewHolder);
            }
            else{
                contactViewHolder = (ViewHolder) convertView.getTag();
            }
            Contact contact = this.mContacts.get(position);
            contactViewHolder.tvContactID.setText(contact.getID());
            contactViewHolder.tvContactFirstName.setText(contact.getFirstName());
            contactViewHolder.tvContactLastName.setText(contact.getLastName());
            return convertView;
        }
        public static class ViewHolder {
            public TextView tvContactID, tvContactFirstName, tvContactLastName;
            public ViewHolder(View contactView) {
                tvContactID = (TextView) contactView.findViewById(R.id.tvContactID);
                tvContactFirstName = (TextView) contactView.findViewById(R.id.tvContactFirstName);
                tvContactLastName = (TextView) contactView.findViewById(R.id.tvContactLastName);
            }
        }
    }
