package com.example.newplayer;



import com.example.newplayer.MediaProvider;

import android.content.Intent;
import android.database.Cursor;

public class AllSongsListActivity extends SongsListActivity {
	
	@Override
	protected Cursor getCursor() {
		return provider.getAllSongsCursor();
	}

	@Override
	protected void addNextExtras(Intent intent) {
		intent.putExtra(MediaProvider.FILTER, MediaProvider.ALL_FILTER);
	}
}
