package com.example.newplayer;


import com.example.newplayer.MediaProvider;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;


public class SongsFromAlbumListActivity extends SongsListActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("cos", "songs from album create");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected Cursor getCursor() {
		long albumId = getIntent().getLongExtra("item", 0);
		return provider.getSongsFromAlbumCursor(albumId);
	}
	
	@Override
	protected void addNextExtras(Intent intent) {
		long albumId = getIntent().getLongExtra("item", 0);
		intent.putExtra(MediaProvider.FILTER, MediaProvider.ALBUM_FILTER);
		intent.putExtra(MediaProvider.ARG, albumId);
	}

}
