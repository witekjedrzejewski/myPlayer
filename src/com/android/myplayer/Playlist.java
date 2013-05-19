package com.android.myplayer;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;

public class Playlist {

	private final static String TAG = "Playlist";
	
	private ArrayList<MediaProvider.Song> songs;
	private int currentSongIndex;
	private int count;
	private boolean isRepeat = false;
	private boolean isShuffle = false;
	private Random random = new Random();
	private Stack<Integer> played = new Stack<Integer>();
	private boolean firstTime = true;
	
	public Playlist(ContentResolver contentResolver, Bundle extras) {
		Log.i(TAG, "creating");
		MediaProvider provider = new MediaProvider(contentResolver);
		String filter = (String) extras.get(MediaProvider.FILTER);
		Long arg = (Long) extras.get(MediaProvider.ARG);
		currentSongIndex = (Integer) extras.get("position");
		Log.i(TAG, filter + " " + arg);
		songs = provider.getSongs(filter, arg);
		count = songs.size();
		Log.i(TAG, "creating done, count = " + count);
	}

	private MediaProvider.Song returnSong(int index) {
		currentSongIndex = index;
		return songs.get(index);
	}
	
	public MediaProvider.Song getNextSong() {
		int index;
		if (firstTime) {
			firstTime = false;
			index = currentSongIndex;
		} else if (isRepeat) {
			index = currentSongIndex;
		} else if (isShuffle) {
			index = random.nextInt(count);
		} else {
			index = (currentSongIndex + 1) % count;
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
		if (isRepeat)
			isShuffle = false;
	}

	public void setShuffle(boolean b) {
		isShuffle = b;
		if (isShuffle)
			isRepeat = false;
	}

}
