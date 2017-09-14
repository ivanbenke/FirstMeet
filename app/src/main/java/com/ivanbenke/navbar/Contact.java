package com.ivanbenke.navbar;

/**
 * Created by ivanb on 13.9.2017..
 */

public class Contact {

        private String mID, mFirstName, mLastName;
        public Contact(String id, String firstName, String lastName) {
            mID = id;
            mFirstName = firstName;
            mLastName = lastName;
        }
        public String getID() { return mID; }
        public String getFirstName() { return mFirstName; }
        public String getLastName() { return mLastName; }
    }
