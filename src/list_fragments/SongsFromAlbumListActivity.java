package list_fragments;


import com.android.myplayer.MediaProvider;

import android.content.Intent;
import android.database.Cursor;

public class SongsFromAlbumListActivity extends SongsListActivity {
	
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
