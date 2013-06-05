package list;

import com.android.myplayer.PlayingActivity;

import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.util.Log;

abstract public class SongsListActivity extends ItemsListActivity {

	private static final String TAG = "SongsListActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected Class<?> getNextActivity() {
		Log.i(TAG, "getNextActivity");
		return PlayingActivity.class;
	}

	protected String[] getFrom() {
		return new String[] {Audio.Media.TITLE};
	}
}