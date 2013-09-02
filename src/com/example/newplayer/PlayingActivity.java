package com.example.newplayer;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newplayer.R;
import com.example.newplayer.FlagDialogFragment.OnFlagSelectedListener;
import com.example.newplayer.R.id;

public class PlayingActivity extends Activity 
	implements OnSeekBarChangeListener, OnFlagSelectedListener {

	private static final String TAG = "PlayingActivity";
	
	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private ImageButton btnPlaylist;
	private ImageButton btnFlags;
	private ImageButton btnRepeat;
	private ImageButton btnShuffle;
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	
	private boolean isPlaying = true;
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	
	private boolean isSeekBroadcastReceiverRegistered = false;
	
	private Utilities utils;
	
	public final static String PREFERENCE_FILE = "com.example.newplayer.Preferences";
	public final static String SELECTED_FLAG_PREFERENCE = "com.example.newplayer.SelectedFlag";
	
	public final static String EXTRA_SEEKPOS = "com.example.newplayer.PlayingActivity.extra_seekpos";
	public final static String EXTRA_IS_REPEAT = "com.example.newplayer.PlayingActivity.extra_is_repeat";
	public final static String EXTRA_IS_SHUFFLE = "com.example.newplayer.PlayingActivity.extra_is_shuffle";
	public final static String EXTRA_NOW_PLAYING = "com.example.newplayer.extra_now_playing";
	public final static String EXTRA_FLAG = "com.example.newplayer.extra_flag";
	
	public final static String ACTION_BROADCAST_SEEKBAR = "com.example.newplayer.PlayingActivity.action.broadcast_seekbar";
	public final static String ACTION_BROADCAST_REPEAT = "com.example.newplayer.PlayingActivity.action.broadcast_repeat";
	public final static String ACTION_BROADCAST_SHUFFLE = "com.example.newplayer.PlayingActivity.action.broadcast_shuffle";
	public final static String ACTION_BROADCAST_FLAG = "com.example.newplayer.PlayingActivity.action.broadcast_flag";
	
	private Intent seekbarBroadcastIntent;
	private Intent repeatBroadcastIntent;
	private Intent shuffleBroadcastIntent;
	private Intent flagBroadcastIntent;
	
	private void startMusicAction(String action) {
		Intent intent = new Intent(action);
		startService(intent);
	}
	
	private void registerSeekBroadcastReceiver() {
		if (!isSeekBroadcastReceiverRegistered) {
			isSeekBroadcastReceiverRegistered = true;
			registerReceiver(broadcastSeekReceiver, 
					new IntentFilter(MusicService.ACTION_BROADCAST_SEEK));
		}
		Log.i(TAG, "seekreceiver registered");
	}

	private void unregisterSeekBroadcastReceiver() {
		if (isSeekBroadcastReceiverRegistered) {
			isSeekBroadcastReceiverRegistered = false;
			unregisterReceiver(broadcastSeekReceiver);
		}
	}
	
	private void registerReceivers() {
		registerReceiver(broadcastTrackDataReceiver, new IntentFilter(MusicService.ACTION_BROADCAST_TRACK_DATA));
		registerSeekBroadcastReceiver();
	}
	
	private void adjustPlayPauseImage() {
		if (isPlaying) {
			btnPlay.setImageResource(R.drawable.btn_pause);
		} else {
			btnPlay.setImageResource(R.drawable.btn_play);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "creating!");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		
		// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
		btnFlags = (ImageButton) findViewById(id.btnTag);
		btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
		btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		
		songCurrentDurationLabel.setText("-:--");
		songTotalDurationLabel.setText("-:--");
		
		utils = new Utilities();
		
		seekbarBroadcastIntent = new Intent(ACTION_BROADCAST_SEEKBAR);
		repeatBroadcastIntent = new Intent(ACTION_BROADCAST_REPEAT);
		shuffleBroadcastIntent = new Intent(ACTION_BROADCAST_SHUFFLE);
		flagBroadcastIntent = new Intent(ACTION_BROADCAST_FLAG);
		
		registerReceivers();

		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this);
		
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.i(TAG, "button play clicked");
				startMusicAction(MusicService.ACTION_TOGGLE_PLAYBACK);
				isPlaying = !isPlaying;
				adjustPlayPauseImage();
			}
		});
		
		btnForward.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startMusicAction(MusicService.ACTION_FORWARD);
			}
		});
		
		btnBackward.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startMusicAction(MusicService.ACTION_BACKWARD);
			}
		});
		
		btnNext.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startMusicAction(MusicService.ACTION_SKIP);
			}
		});
		
		btnPrevious.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startMusicAction(MusicService.ACTION_PREVIOUS);
			}
		});
		
		btnRepeat.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(isRepeat){
					isRepeat = false;
					Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
					btnRepeat.setImageResource(R.drawable.btn_repeat);
					
				}else{
					// make repeat to true
					isRepeat = true;
					Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isShuffle = false;
					btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}
				repeatBroadcastIntent.putExtra(EXTRA_IS_REPEAT, isRepeat);
				sendBroadcast(repeatBroadcastIntent);
			}
		});
		
		btnShuffle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(isShuffle){
					isShuffle = false;
					Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}else{
					// make repeat to true
					isShuffle= true;
					Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isRepeat = false;
					btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				}
				shuffleBroadcastIntent.putExtra(EXTRA_IS_SHUFFLE, isShuffle);
				sendBroadcast(shuffleBroadcastIntent);
			}
		});
		
		btnPlaylist.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), MainActivity.class);
				startActivityForResult(i, 100);			
			}
		});
		
		btnFlags.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FlagDialogFragment dialog = new FlagDialogFragment();
			    dialog.show(getFragmentManager(), "abc");
			}
		});

		Bundle extras = getIntent().getExtras();
		
		if (extras.getBoolean(EXTRA_NOW_PLAYING, false)) { //only if came to see "now playing"
			
			startMusicAction(MusicService.ACTION_BROADCAST_TRACK_DATA);
			
		} else {
			
			Intent intent = new Intent(MusicService.ACTION_CHANGE_PLAYLIST);
	    	
			Log.i(TAG, getIntent().getExtras().toString());
	    	Log.i(TAG, getIntent().getExtras().getString(MediaProvider.FILTER));
	    	
	    	intent.putExtras(extras);
	    	startService(intent);
		}
	}
	
	@Override
	public void onResume() {
		Log.i(TAG, "resume!");
		super.onResume();
		registerReceivers();
	}
	
	private BroadcastReceiver broadcastTrackDataReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "TrackData onReceive()");
			String title = intent.getStringExtra(MusicService.EXTRA_TITLE);
			int duration = intent.getIntExtra(MusicService.EXTRA_DURATION, 0);
			int pos = intent.getIntExtra(MusicService.EXTRA_POS, 0);
			boolean playing = intent.getBooleanExtra(MusicService.EXTRA_PLAYING, false);
			songTitleLabel.setText(title);
			songProgressBar.setMax(duration);
			songProgressBar.setProgress(pos);
			songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(pos));
			songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(duration));

			isPlaying = playing;
			adjustPlayPauseImage();
		}

	};

	private BroadcastReceiver broadcastSeekReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.i(TAG, "Seek onReceive()");
			int currentPosition = intent.getIntExtra(MusicService.EXTRA_CURRENT_POSITION, 0);
			songProgressBar.setProgress(currentPosition);
			songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentPosition));
		}

	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		unregisterSeekBroadcastReceiver();
    }

	@Override
    public void onStopTrackingTouch(SeekBar seekBar) {
		int seekPos = seekBar.getProgress();
		seekbarBroadcastIntent.putExtra(EXTRA_SEEKPOS, seekPos);
		registerSeekBroadcastReceiver(); //order important - we wait for UI update
		sendBroadcast(seekbarBroadcastIntent);
    }
	
	@Override
	public void onStop() {
		unregisterSeekBroadcastReceiver();
		unregisterReceiver(broadcastTrackDataReceiver);
		super.onStop();
	}

	@Override
	public void onFlagSelected(int flag) {
		Log.i(TAG, "selected " + flag);
		getSharedPreferences(PREFERENCE_FILE, 0)
			.edit().putInt(SELECTED_FLAG_PREFERENCE, flag).commit();
		
		flagBroadcastIntent.putExtra(EXTRA_FLAG, flag);
		sendBroadcast(flagBroadcastIntent);
	}
}