package com.example.newplayer;

import android.database.Cursor;

public class AllAlbumsListActivity extends AlbumsListActivity {
	@Override
	protected Cursor getCursor() {
		return provider.getAllAlbumsCursor();
	}

}
