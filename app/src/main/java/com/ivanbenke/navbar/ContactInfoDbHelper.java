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

    String id;

    private static ContactInfoDbHelper mContactInfoDbHelper = null;

    public ContactInfoDbHelper(Context context) {
        super(context.getApplicationContext(), Schema.DATABASE_NAME, null, Schema.DATABASE_VERSION);
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

    static final String CREATE_TABLE_CONTACT_INFO = "CREATE TABLE " + Schema.TABLE_CONTACT_INFO + " (" + Schema.ID + " TEXT," + Schema.FIRST_NAME + " TEXT," + Schema.LAST_NAME + " TEXT," + Schema.PHONE_NUMBER + " TEXT," + Schema.LATITUDE + " TEXT," + Schema.LONGITUDE + " TEXT);";
    static final String DROP_TABLE_CONTACT_INFO = "DROP TABLE IF EXISTS " + Schema.TABLE_CONTACT_INFO;
    static final String SELECT_ALL_CONTACTS = "SELECT " + Schema.ID + "," + Schema.FIRST_NAME + "," + Schema.LAST_NAME + " FROM " + Schema.TABLE_CONTACT_INFO;

    public void insertInfo(String first_name, String last_name, String phone_number, String latitude, String longitude){
        id = first_name + last_name + phone_number;
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.ID, id);
        contentValues.put(Schema.FIRST_NAME, first_name);
        contentValues.put(Schema.LAST_NAME, last_name);
        contentValues.put(Schema.PHONE_NUMBER, phone_number);
        contentValues.put(Schema.LATITUDE, latitude);
        contentValues.put(Schema.LONGITUDE, longitude);
        SQLiteDatabase writeableDatabase = this.getWritableDatabase();
        /*String count = "SELECT count(*) FROM " + Schema.TABLE_CONTACT_INFO + " WHERE " + Schema.ID + " = " + id;
        Cursor mcursor = writeableDatabase.rawQuery(count, null);
        mcursor.moveToFirst();
        String select = Schema.ID + " = " + id;
        int icount = mcursor.getInt(0);
        if (icount > 0 ) {
            writeableDatabase.update(Schema.TABLE_CONTACT_INFO, contentValues, select, null);
        } else {
            writeableDatabase.insert(Schema.TABLE_CONTACT_INFO, null, contentValues);
        }
        mcursor.close();*/
        writeableDatabase.replace(Schema.TABLE_CONTACT_INFO, null, contentValues);
        writeableDatabase.close();
    }

    public ArrayList<Contact> getAllContacts(){
        SQLiteDatabase writeableDatabase = this.getWritableDatabase();
        Cursor contactInfoCursor = writeableDatabase.rawQuery(SELECT_ALL_CONTACTS, null);
        ArrayList<Contact> contacts = new ArrayList<>();
        if(contactInfoCursor.moveToFirst()){
            do {
                String id = contactInfoCursor.getString(0);
                String first_name = contactInfoCursor.getString(1);
                String last_name = contactInfoCursor.getString(2);
                if (!id.equals("nullnullnull")) {
                    contacts.add(new Contact(id, first_name, last_name));
                }
            } while(contactInfoCursor.moveToNext());
        }
        contactInfoCursor.close();
        writeableDatabase.close();
        //return contactInfo;
        return contacts;
    }

    public ContactInfo getContact(String contactID){
        SQLiteDatabase writeableDatabase = this.getWritableDatabase();
        String SELECT_CONTACT = "SELECT " + Schema.ID + "," + Schema.FIRST_NAME + "," + Schema.LAST_NAME + "," + Schema.PHONE_NUMBER + "," + Schema.LATITUDE + "," + Schema.LONGITUDE + " FROM " + Schema.TABLE_CONTACT_INFO + " WHERE " + Schema.ID + " = ?";
        Cursor contactInfoCursor = writeableDatabase.rawQuery(SELECT_CONTACT, new String[] {contactID});
        if(contactInfoCursor != null) {
            contactInfoCursor.moveToFirst();
        }
        String id = contactInfoCursor.getString(0);
        String first_name = contactInfoCursor.getString(1);
        String last_name = contactInfoCursor.getString(2);
        String phone_number = contactInfoCursor.getString(3);
        String latitude = contactInfoCursor.getString(4);
        String longitude = contactInfoCursor.getString(5);
        ContactInfo contact = new ContactInfo(id, first_name, last_name, phone_number, latitude, longitude);
        contactInfoCursor.close();
        writeableDatabase.close();
        return contact;
    }

    public static class Schema {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "ContactInfo.db";
        static final String TABLE_CONTACT_INFO = "contact_info";
        static final String ID = "id";
        static final String FIRST_NAME = "first_name";
        static final String LAST_NAME = "last_name";
        static final String PHONE_NUMBER = "phone_number";
        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
    }
}
