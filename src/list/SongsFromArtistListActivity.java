package list;


import com.android.myplayer.MediaProvider;

import android.content.Intent;
import android.database.Cursor;

public class SongsFromArtistListActivity extends SongsListActivity {
	
	@Override
	protected Cursor getCursor() {
		long artistId = getIntent().getLongExtra("item", 0);
		return provider.getSongsFromArtistCursor(artistId);
	}
	
	@Override
	protected void addNextExtras(Intent intent) {
		long artistId = getIntent().getLongExtra("item", 0);
		intent.putExtra(MediaProvider.FILTER, MediaProvider.ARTIST_FILTER);
		intent.putExtra(MediaProvider.ARG, artistId);
		//TODO
	}

}
