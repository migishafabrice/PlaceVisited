package com.myapp.placevisitedapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PlacesDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PLACES = "places";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLACES_TABLE = "CREATE TABLE " + TABLE_PLACES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT,"
                + KEY_LATITUDE + " REAL,"
                + KEY_LONGITUDE + " REAL,"
                + KEY_DATE + " TEXT"
                + ")";
        db.execSQL(CREATE_PLACES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        onCreate(db);
    }

    public long addPlace(String title, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put(KEY_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date()));
        long id = db.insert(TABLE_PLACES, null, values);
        db.close();
        return id;
    }

    public void updatePlaceTitle(long id, String newTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, newTitle);
        db.update(TABLE_PLACES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Place> getAllPlaces() {
        List<Place> placesList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PLACES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Place place = new Place(
                        cursor.getLong(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE))
                );
                placesList.add(place);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return placesList;
    }

    public void deletePlace(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLACES, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLACES, null, null);
        db.close();
    }
}