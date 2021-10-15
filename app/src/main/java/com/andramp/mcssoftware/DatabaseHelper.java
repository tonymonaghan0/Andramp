package com.andramp.mcssoftware;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.andramp.mcssoftware.MainActivity.APP_FILES_PATH;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "andramp.db";
    private static final int VERSION = 3;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create two tables
        db.execSQL("CREATE TABLE TRACKS( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, path TEXT);");
        db.execSQL("CREATE TABLE SETTINGS( _id INTEGER PRIMARY KEY AUTOINCREMENT, setting TEXT, value TEXT);");

        //Insert the settings fields and set to default setting
        ContentValues values = new ContentValues();
        values.put("setting", "manual");
        values.put("value", "false");
        db.insert("settings", null , values);
        values.put("setting", "screen");
        values.put("value", "false");
        db.insert("settings", null , values);
        values.put("setting", "landscape");
        values.put("value", "false");
        db.insert("settings", null , values);
        values.put("setting", "portrait");
        values.put("value", "false");
        db.insert("settings", null , values);
        values.put("setting", "home");
        values.put("value", APP_FILES_PATH);
        db.insert("settings", null , values);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //On database upgrade drop all tables
        db.execSQL("DROP TABLE IF EXISTS TRACKS;");
        db.execSQL("DROP TABLE IF EXISTS SETTINGS;");
        onCreate(db);
    }
}
