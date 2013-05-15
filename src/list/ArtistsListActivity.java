package list;


import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore.Audio;
import android.util.Log;

public class ArtistsListActivity extends ItemsListActivity {

	private static final String tag = "ArtistsListActivity";
	
	@Override
	protected Cursor getCursor() {
		Log.i(tag, "getCursor()");
		return provider.getAllArtistsCursor();
	}

	@Override
	protected Class<?> getNextActivity() {
		Log.i(tag, "getNextActivity()");
		return AlbumsListActivity.class;
	}

	@Override
	protected String[] getFrom() {
		return new String[]{Audio.Artists.ARTIST};
	}

	@Override
	protected void addNextExtras(Intent intent) {
	}
	
	
}
