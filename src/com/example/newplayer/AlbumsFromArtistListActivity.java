package com.example.newplayer;


import android.database.Cursor;

public class AlbumsFromArtistListActivity extends AlbumsListActivity {
	@Override
	protected Cursor getCursor() {
		long artistId = getIntent().getLongExtra("item", 0);
		return provider.getAlbumsFromArtistCursor(artistId);
	}

}
