package com.cylim.saferide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by marco on 29/4/15.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "db_report";
    private static final String TABLE_REPORT = "tb_reports";
    private static final String KEY_ID = "id";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_TIME = "time";

    //secondary database for local cache to improve quality
    private static final String TABLE_CACHE = "tb_cache";
    private static final String KEY_CACHE_ID = "id";
    private static final String KEY_CACHE_LAT = "lat";
    private static final String KEY_CACHE_LNG = "lng";
    private static final String KEY_CACHE_BY = "by";
    private static final String KEY_CACHE_TYPE = "type";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_TABLE_REPORT = "CREATE TABLE " + TABLE_REPORT + "("
                    + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_LAT
                    + " TEXT," + KEY_LNG + " TEXT," + KEY_TIME
                    + " TEXT)";

            db.execSQL(CREATE_TABLE_REPORT);

            String CREATE_TABLE_CACHE = "CREATE TABLE " + TABLE_CACHE + "(" + KEY_CACHE_ID
                    + " INTEGER PRIMARY KEY, " + KEY_CACHE_LAT + " TEXT,"
                    + KEY_CACHE_LNG + " TEXT," + KEY_CACHE_BY + " TEXT,"
                    + KEY_CACHE_TYPE + " TEXT)";

            db.execSQL(CREATE_TABLE_CACHE);

            Log.d("SQL", "Creation Completed.");

        } catch (Exception e) {
            Log.d("CREATION ERROR", e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHE);
        onCreate(db);
    }

    public void addReport(String lat, String lng, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LAT, lat);
        values.put(KEY_LNG, lng);
        values.put(KEY_TIME, time);
        db.insert(TABLE_REPORT, null, values);
        db.close(); // Closing database connection

        Log.d("AddedReport", time + " " + lat + "," + lng);
    }


    public String[][] getReports() {

        String selectQuery = "SELECT  * FROM " + TABLE_REPORT;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String[][] array = new String[cursor.getCount()][4];
        int i = 0;
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(KEY_ID));
            String lat = cursor.getString(cursor.getColumnIndex(KEY_LAT));
            String lng = cursor.getString(cursor.getColumnIndex(KEY_LNG));
            String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
            array[i][0] = id;
            array[i][1] = lat;
            array[i][2] = lng;
            array[i][3] = time;
            i++;
        }
        db.close();
        return array;

    }


    public void deleteReport(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_REPORT + " WHERE " + KEY_ID
                + " = '" + id + "'");
        db.close();
    }

    public void cacheAddReport(int id, String lat, String lng, String by, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CACHE_ID, id);
        values.put(KEY_CACHE_LAT, lat);
        values.put(KEY_CACHE_LNG, lng);
        values.put(KEY_CACHE_BY, by);
        values.put(KEY_CACHE_TYPE, type);
        db.insert(TABLE_CACHE, null, values);
        db.close(); // Closing database connection
    }

    public int getLastCachedReport() {


        String selectQuery = "SELECT * FROM " + TABLE_CACHE + " ORDER BY " + KEY_CACHE_ID + " DESC LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int result = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
        }
        db.close();
        Log.d("LastCachedReport", String.valueOf(result));
        return result;
    }

    public String[][] getCachedReports() {

        String selectQuery = "SELECT * FROM " + TABLE_CACHE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String[][] array = new String[cursor.getCount()][5];
        int i = 0;
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(KEY_CACHE_ID));
            String lat = cursor.getString(cursor.getColumnIndex(KEY_CACHE_LAT));
            String lng = cursor.getString(cursor.getColumnIndex(KEY_CACHE_LNG));
            String by = cursor.getString(cursor.getColumnIndex(KEY_CACHE_BY));
            String type = cursor.getString(cursor.getColumnIndex(KEY_CACHE_TYPE));
            array[i][0] = id;
            array[i][1] = lat;
            array[i][2] = lng;
            array[i][3] = by;
            array[i][4] = type;
            i++;
        }
        db.close();
        return array;

    }

    public void clearCachedReport() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CACHE);
        db.close();
    }
}
