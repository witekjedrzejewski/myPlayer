package com.example.newplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class SongDatabaseHelper extends SQLiteOpenHelper {

	class Row extends Object {
        public long id; //id on sdcard
        public long played; //how many times has been played
        public long skipped; //how many times has been skipped
        public long started; //how many times was clicked
        public long repeated; //how many times repeat was chosen
        
        public Row(long id) {
        	this.id = id;
        	played = 0;
        	skipped = 0;
        	started = 0;
        	repeated = 0;
        }
        
        public String toString() {
        	return id + ":" + played + "/" + skipped 
        			+ "/" + started + "/" + repeated;
        }
    }
	
	private static final String TAG = "DBHelper";
	
    private static final String DATABASE_NAME = "SMARTPLAYERDB";
    private static final String DATABASE_TABLE = "SONGSRATINGS";
    private static final int DATABASE_VERSION = 1;
    
    private static final String KEY_ID = "id";
    private static final String KEY_PLAYED = "played";
    private static final String KEY_SKIPPED = "skipped";
    private static final String KEY_STARTED = "started";
    private static final String KEY_REPEATED = "repeated";

    public SongDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		String DATABASE_CREATE =
		        "create table " + DATABASE_TABLE
	    		+ "(" + KEY_ID + " integer primary key, "
	            + KEY_PLAYED + " integer, "
	            + KEY_SKIPPED + " integer, "
	            + KEY_STARTED + " integer, "
	            + KEY_REPEATED + " integer);";
		db.execSQL(DATABASE_CREATE);
		Log.i(TAG, "database created" + DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		onCreate(db);
	}
	
	void addRows(HashMap<Long, Row> rows) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        for (Row row : rows.values()) {
	        ContentValues values = new ContentValues();
	        values.put(KEY_ID, row.id);
	        values.put(KEY_PLAYED, row.played);
	        values.put(KEY_SKIPPED, row.skipped);
	        values.put(KEY_STARTED, row.started);
	        values.put(KEY_REPEATED, row.repeated);
		       
	        db.insertWithOnConflict(DATABASE_TABLE, null, values, 
	        		SQLiteDatabase.CONFLICT_REPLACE);
	        
	        Log.i(TAG, "added/replaced " + row);
        }
        db.close();
    }
	
	/* returns all ratings for songs listed in given set */
	HashMap<Long, Row> getRatingsForSongs(ArrayList<Long> songIds) {
		HashMap<Long, Row> result = new HashMap<Long, Row>();
		HashMap<Long, Row> allRows = new HashMap<Long, Row>();
		String selectQuery = "SELECT * FROM " + DATABASE_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // adding to list selected rows
        if (cursor.moveToFirst()) {
            do {
            	long id = Long.parseLong(cursor.getString(0));
                Row row = new Row(id);
                row.played = Long.parseLong(cursor.getString(1));
                row.skipped = Long.parseLong(cursor.getString(2));
                row.started = Long.parseLong(cursor.getString(3));
                row.repeated = Long.parseLong(cursor.getString(4));
                allRows.put(row.id, row);
            } while (cursor.moveToNext());
        }
        for (Long songId : songIds) {
        	if (allRows.containsKey(songId)) {
        		result.put(songId, allRows.get(songId));
        	} else {
        		result.put(songId, new Row(songId));
        	}
        }
		return result;
	}
}
