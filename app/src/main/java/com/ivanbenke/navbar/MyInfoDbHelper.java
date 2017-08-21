package com.ivanbenke.navbar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivanb on 20.8.2017..
 */

public class MyInfoDbHelper extends SQLiteOpenHelper {

    private static MyInfoDbHelper mMyInfoDbHelper = null;

    public MyInfoDbHelper(Context context) {
        super(context.getApplicationContext(), Schema.DATABASE_NAME, null, Schema.DATABASE_VERSION);
    }

    public static synchronized MyInfoDbHelper getInstance(Context context){
        if(mMyInfoDbHelper == null){
            mMyInfoDbHelper = new MyInfoDbHelper(context);
        }
        return mMyInfoDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MY_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_MY_BOOKS);
        this.onCreate(db);
    }

    static final String CREATE_TABLE_MY_INFO = "CREATE TABLE " + Schema.TABLE_MY_INFO + " (" + Schema.FIRST_NAME + " TEXT," + Schema.LAST_NAME + " TEXT," + Schema.PHONE_NUMBER + " INTEGER);";
    static final String DROP_TABLE_MY_BOOKS = "DROP TABLE IF EXISTS " + Schema.TABLE_MY_INFO;
    static final String SELECT_MY_INFO = "SELECT " + Schema.FIRST_NAME + "," + Schema.LAST_NAME + "," + Schema.PHONE_NUMBER + " FROM " + Schema.TABLE_MY_INFO;

    public void insertInfo(String first_name, String last_name, String phone_number){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.FIRST_NAME, first_name);
        contentValues.put(Schema.LAST_NAME, last_name);
        contentValues.put(Schema.PHONE_NUMBER, phone_number);
        SQLiteDatabase writeableDatabase = this.getWritableDatabase();
        String count = "SELECT count(*) FROM " + Schema.TABLE_MY_INFO;
        Cursor mcursor = writeableDatabase.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if (icount > 0 ) {
            writeableDatabase.update(Schema.TABLE_MY_INFO, contentValues, null, null);
        } else {
            writeableDatabase.insert(Schema.TABLE_MY_INFO, null, contentValues);
        }
        writeableDatabase.close();
    }

    public List<String> getMyInfo(){
        SQLiteDatabase writeableDatabase = this.getWritableDatabase();
        Cursor myInfoCursor = writeableDatabase.rawQuery(SELECT_MY_INFO, null);
        List<String> myInfo = new ArrayList<String>();
        if(myInfoCursor.moveToFirst()){
            do {
                String first_name = myInfoCursor.getString(0);
                String last_name = myInfoCursor.getString(1);
                String phone_number = myInfoCursor.getString(2);
                myInfo.add(first_name);
                myInfo.add(last_name);
                myInfo.add(phone_number);
            } while(myInfoCursor.moveToNext());
        }
        myInfoCursor.close();
        writeableDatabase.close();
        return myInfo;
    }

    public static class Schema {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "MyInfo.db";
        static final String TABLE_MY_INFO = "my_info";
        static final String FIRST_NAME = "first_name";
        static final String LAST_NAME = "last_name";
        static final String PHONE_NUMBER = "phone_number";
    }
}
