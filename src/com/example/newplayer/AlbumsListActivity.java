package com.example.newplayer;


import android.content.Intent;
import android.provider.MediaStore.Audio;
import android.util.Log;

abstract public class AlbumsListActivity extends ItemsListActivity {
	
	private static final String TAG = "AlbumsListActivity";
	
	@Override
	protected Class<?> getNextActivity() {
		Log.i(TAG, "getNextActivity()");
		return SongsFromAlbumListActivity.class;
	}

	protected String[] getFrom() {
		return new String[] {Audio.Albums.ALBUM};
	}

	@Override
	protected void addNextExtras(Intent intent) {
	}
}

