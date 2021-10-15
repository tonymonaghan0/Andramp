package com.andramp.mcssoftware;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class DatabaseAdapter {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    DatabaseAdapter(Context context) {
        //Create a new DatabaseHelper dbHelper
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

/*    DatabaseAdapter open() {
        database = dbHelper.getWritableDatabase();
        return this;
    }*/

    void open() {
        database = dbHelper.getWritableDatabase();
    }

    void close() {
        dbHelper.close();
        dbHelper = null;
    }

    /*private Cursor getAllTrackEntries() {
        String[] columns = new String[2];
        columns[0] = "name";
        columns[1] = "path";
        return database.query("tracks", columns, null, null, null, null, null);
    }

    public List<String> getAllTracks() {
        ArrayList<String> tracks = new ArrayList<String>();
        Cursor cursor = getAllTrackEntries();
        if (cursor.moveToFirst()) {
            do {
                tracks.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tracks;
    }

    public boolean exists(String name) {
        Cursor cursor = database.rawQuery(
                "select name from tracks where name=?",
                new String[]{name});
        boolean result = cursor.getCount() >= 1;
        cursor.close();
        return result;
    }

    public long insertTrack(String name, String path) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("path", path);
        return database.insert("tracks", null, values);
    }

    public int deleteTrack(String name) {
        String whereClause = "name = ?";
        String[] whereArgs = new String[1];
        whereArgs[0] = name;
        return database.delete("tracks", whereClause, whereArgs);
    }

    public int deleteAllTracks() {
        return database.delete("tracks", null, null);
    }

    public int updateTrack(String name, String path) {
        String whereClause = "name = ?";
        String[] whereArgs = new String[2];
        whereArgs[0] = name;
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("path", path);
        return database.update("tracks", values, whereClause, whereArgs);
    }*/

    String getSetting(String s) {

        //Returns the value of fields

        Cursor cursor = database.rawQuery(
                "select value from settings where setting=?",
                new String[]{s});
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            String result = cursor.getString(cursor.getColumnIndex("value"));
            cursor.close();
            return result;
        }else{
            return null;
        }
    }


/*    int updateSetting(String s, String v) {

        //Will find the field s and update value to v

        String whereClause = "setting = ?";
        String[] whereArgs = new String[1];
        whereArgs[0] = s;
        ContentValues values = new ContentValues();
        values.put("value", v);
        return database.update("settings", values, whereClause , whereArgs);

    }*/

    void updateSetting(String s, String v) {

        //Will find the field s and update value to v

        String whereClause = "setting = ?";
        String[] whereArgs = new String[1];
        whereArgs[0] = s;
        ContentValues values = new ContentValues();
        values.put("value", v);
        database.update("settings", values, whereClause, whereArgs);

    }

}
