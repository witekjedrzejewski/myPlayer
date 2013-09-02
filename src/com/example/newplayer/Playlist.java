package com.example.newplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.example.newplayer.SongDatabaseHelper.Row;

public class Playlist {

	private final static String TAG = "Playlist";
	
	private ArrayList<MediaProvider.Song> songs;
	private RatingsSet ratingsSet;
	private int currentSongIndex;
	private int count;
	private boolean isRepeat = false;
	private boolean isShuffle = false;
	private Random random = new Random();
	// for retrieving previous songs
	private Stack<Integer> played = new Stack<Integer>();
	private boolean firstTime = true;
	private String[] flags;
	
	private ArrayList<Long> idsFromSongs(ArrayList<MediaProvider.Song> songs) {
		ArrayList<Long> result = new ArrayList<Long>();
		for (MediaProvider.Song song : songs) {
			result.add(song.getId());
		}
		return result;
	}
	
	public Playlist(Context context, ContentResolver contentResolver, Bundle extras) {
		Log.i(TAG, "creating");
		MediaProvider provider = new MediaProvider(contentResolver);
		String filter = (String) extras.get(MediaProvider.FILTER);
		Long arg = (Long) extras.get(MediaProvider.ARG);
		currentSongIndex = (Integer) extras.get("position");
		Log.i(TAG, filter + " " + arg);
		songs = provider.getSongs(filter, arg);
		ratingsSet = new RatingsSet(context, songs);
		flags = context.getResources().getStringArray(R.array.flags_array);
		count = songs.size();
		Log.i(TAG, "creating done, count = " + count);
	}

	private MediaProvider.Song returnSong(int index) {
		currentSongIndex = index;
		
		MediaProvider.Song chosenSong = songs.get(index);
		ratingsSet.songPlayed(chosenSong.getId());
		return chosenSong;
	}
	
	public MediaProvider.Song getNextSong(boolean forceSkipped) {
		int index;
		if (firstTime) {
			firstTime = false;
			index = currentSongIndex;
			ratingsSet.songStarted(songs.get(index).getId());
		} else {
			if (forceSkipped) {
				ratingsSet.songSkipped(songs.get(currentSongIndex).getId());
			}
			if (isRepeat) {
				index = currentSongIndex;
			} else if (isShuffle) {
				index = random.nextInt(count);
			} else { //normal order
				index = (currentSongIndex + 1) % count;
			}
		}
		played.add(index);
		return returnSong(index);
	}
	
	public MediaProvider.Song getPreviousSong() {
		int index;
		if (isRepeat)
			index = currentSongIndex;
		else if (played.empty())
			index = 0;
		else
			index = played.pop();
		return returnSong(index);
	}
	
	public void setRepeat(boolean b) {
		isRepeat = b;
		ratingsSet.songRepeated(songs.get(currentSongIndex).getId());
		if (isRepeat)
			isShuffle = false;
	}

	public void setShuffle(boolean b) {
		isShuffle = b;
		if (isShuffle)
			isRepeat = false;
	}
	
	public void setFlag(int f) {
		String flag = flags[f];
		
	}
	
	public void sendUpdateToDB() {
		ratingsSet.sendUpdateToDB();
	}

}
