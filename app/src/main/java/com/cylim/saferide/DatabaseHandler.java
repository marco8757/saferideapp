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


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_TABLE_REPORT = "CREATE TABLE " + TABLE_REPORT + "("
                    + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_LAT
                    + " DOUBLE," + KEY_LNG + " DOUBLE," + KEY_TIME
                    + " TEXT)";

            db.execSQL(CREATE_TABLE_REPORT);

            db.execSQL("INSERT INTO " + TABLE_REPORT + " VALUES ('F-','-1.0','0')");

        } catch (Exception e) {
            Log.d("CREATION ERROR", e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORT);
        onCreate(db);
    }


    public void addReport(double lat, double lng, String time) {
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
}
