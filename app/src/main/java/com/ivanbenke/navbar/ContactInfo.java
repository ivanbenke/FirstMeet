package com.ivanbenke.navbar;

/**
 * Created by ivanb on 13.9.2017..
 */

public class ContactInfo {

    private String mID, mFirstName, mLastName, mPhoneNumber, mLatitude, mLongitude;

    public ContactInfo(String id, String firstName, String lastName, String phoneNumber, String latitude, String longitude) {
        mID = id;
        mFirstName = firstName;
        mLastName = lastName;
        mPhoneNumber = phoneNumber;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getID() { return mID; }
    public String getFirstName() { return mFirstName; }
    public String getLastName() { return mLastName; }
    public String getPhoneNumber() { return mPhoneNumber; }
    public String getLatitude() { return mLatitude; }
    public String getLongitude() { return mLongitude; }
}
