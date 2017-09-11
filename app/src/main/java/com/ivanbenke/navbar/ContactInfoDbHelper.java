package com.ivanbenke.navbar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivanb on 11.9.2017..
 */

public class ContactInfoDbHelper extends SQLiteOpenHelper {

    private static ContactInfoDbHelper mContactInfoDbHelper = null;

    public ContactInfoDbHelper(Context context) {
        super(context.getApplicationContext(), ContactInfoDbHelper.Schema.DATABASE_NAME, null, ContactInfoDbHelper.Schema.DATABASE_VERSION);
    }

    public static synchronized ContactInfoDbHelper getInstance(Context context){
        if(mContactInfoDbHelper == null){
            mContactInfoDbHelper = new ContactInfoDbHelper(context);
        }
        return mContactInfoDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACT_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_CONTACT_INFO);
        this.onCreate(db);
    }

    static final String CREATE_TABLE_CONTACT_INFO = "CREATE TABLE " + ContactInfoDbHelper.Schema.TABLE_CONTACT_INFO + " (" + ContactInfoDbHelper.Schema.FIRST_NAME + " TEXT," + ContactInfoDbHelper.Schema.LAST_NAME + " TEXT," + ContactInfoDbHelper.Schema.PHONE_NUMBER + " TEXT," + ContactInfoDbHelper.Schema.LATITUDE + " TEXT," + ContactInfoDbHelper.Schema.LONGITUDE + " TEXT);";
    static final String DROP_TABLE_CONTACT_INFO = "DROP TABLE IF EXISTS " + ContactInfoDbHelper.Schema.TABLE_CONTACT_INFO;
    static final String SELECT_CONTACT_INFO = "SELECT " + ContactInfoDbHelper.Schema.FIRST_NAME + "," + ContactInfoDbHelper.Schema.LAST_NAME + "," + ContactInfoDbHelper.Schema.PHONE_NUMBER + "," + ContactInfoDbHelper.Schema.LATITUDE + "," + ContactInfoDbHelper.Schema.LONGITUDE + " FROM " + ContactInfoDbHelper.Schema.TABLE_CONTACT_INFO;

    public void insertInfo(String first_name, String last_name, String phone_number, String latitude, String longitude){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactInfoDbHelper.Schema.FIRST_NAME, first_name);
        contentValues.put(ContactInfoDbHelper.Schema.LAST_NAME, last_name);
        contentValues.put(ContactInfoDbHelper.Schema.PHONE_NUMBER, phone_number);
        contentValues.put(ContactInfoDbHelper.Schema.LATITUDE, latitude);
        contentValues.put(ContactInfoDbHelper.Schema.LONGITUDE, longitude);
        SQLiteDatabase writeableDatabase = this.getWritableDatabase();
        String count = "SELECT count(*) FROM " + ContactInfoDbHelper.Schema.TABLE_CONTACT_INFO;
        Cursor mcursor = writeableDatabase.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if (icount > 0 ) {
            writeableDatabase.update(ContactInfoDbHelper.Schema.TABLE_CONTACT_INFO, contentValues, null, null);
        } else {
            writeableDatabase.insert(ContactInfoDbHelper.Schema.TABLE_CONTACT_INFO, null, contentValues);
        }
        mcursor.close();
        writeableDatabase.close();
    }

    public List<String> getContactInfo(){
        SQLiteDatabase writeableDatabase = this.getWritableDatabase();
        Cursor contactInfoCursor = writeableDatabase.rawQuery(SELECT_CONTACT_INFO, null);
        List<String> contactInfo = new ArrayList<String>();
        if(contactInfoCursor.moveToFirst()){
            do {
                String first_name = contactInfoCursor.getString(0);
                String last_name = contactInfoCursor.getString(1);
                String phone_number = contactInfoCursor.getString(2);
                String latitude = contactInfoCursor.getString(3);
                String longitude = contactInfoCursor.getString(4);
                contactInfo.add(first_name);
                contactInfo.add(last_name);
                contactInfo.add(phone_number);
                contactInfo.add(latitude);
                contactInfo.add(longitude);
            } while(contactInfoCursor.moveToNext());
        }
        contactInfoCursor.close();
        writeableDatabase.close();
        return contactInfo;
    }

    public static class Schema {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "ContactInfo.db";
        static final String TABLE_CONTACT_INFO = "contact_info";
        static final String FIRST_NAME = "first_name";
        static final String LAST_NAME = "last_name";
        static final String PHONE_NUMBER = "phone_number";
        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
    }
}
