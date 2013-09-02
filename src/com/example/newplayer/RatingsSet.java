package com.example.newplayer;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.example.newplayer.MediaProvider.Song;
import com.example.newplayer.SongDatabaseHelper.Row;

public class RatingsSet {

	private final static String TAG = "RatingsSet";
	
	private ArrayList<Song> songs;
	private SongDatabaseHelper dbHelper;
	private HashMap<Long, Row> ratings;
	
	private ArrayList<Long> idsFromSongs(ArrayList<Song> songs) {
		ArrayList<Long> result = new ArrayList<Long>();
		for (Song song : songs) {
			result.add(song.getId());
		}
		return result;
	}
	
	public RatingsSet(Context context, ArrayList<Song> songs) {
		Log.i(TAG, "creating");
		dbHelper = new SongDatabaseHelper(context);
		this.songs = songs;
		ratings = dbHelper.getRatingsForSongs(idsFromSongs(songs));
	}

	public void songPlayed(long songId) {
		Row row = ratings.get(songId);
		row.played++;
		ratings.put(songId, row);
	}
	
	public void songStarted(long songId) {
		Row row = ratings.get(songId);
		row.started++;
		ratings.put(songId, row);
	}
	
	public void songSkipped(long songId) {
		Row row = ratings.get(songId);
		row.skipped++;
		ratings.put(songId, row);
	}
	
	public void songRepeated(long songId) {
		Row row = ratings.get(songId);
		row.repeated++;
		ratings.put(songId, row);
	}
	
	public void sendUpdateToDB() {
		dbHelper.addRows(ratings);
	}
}
