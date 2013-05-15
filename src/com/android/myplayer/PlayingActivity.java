package com.android.myplayer;

import list.ArtistsListActivity;
import android.app.Activity;
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

import com.androidhive.musicplayer.R;

public class PlayingActivity extends Activity implements OnSeekBarChangeListener {

	private static final String TAG = "PlayingActivity";
	
	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private ImageButton btnPlaylist;
	private ImageButton btnRepeat;
	private ImageButton btnShuffle;
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	
	private boolean isPlaying = true;
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	
	private Utilities utils;
	
	public final static String EXTRA_SEEKPOS = 
			"com.android.myplayer.PlayingActivity.extra_seekpos";
	public final static String EXTRA_IS_REPEAT = 
			"com.android.myplayer.PlayingActivity.extra_is_repeat";
	public final static String EXTRA_IS_SHUFFLE = 
			"com.android.myplayer.PlayingActivity.extra_is_shuffle";
	
	public final static String ACTION_BROADCAST_SEEKBAR = 
			"com.android.myplayer.PlayingActivity.action.broadcast_seekbar";
	public final static String ACTION_BROADCAST_REPEAT = 
			"com.android.myplayer.PlayingActivity.action.broadcast_repeat";
	public final static String ACTION_BROADCAST_SHUFFLE = 
			"com.android.myplayer.PlayingActivity.action.broadcast_shuffle";
	
	private Intent seekbarBroadcastIntent;
	private Intent repeatBroadcastIntent;
	private Intent shuffleBroadcastIntent;
	
	private boolean isSeekBroadcastReceiverRegistered = false;
	
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		
		// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
		btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
		btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		
		utils = new Utilities();
		
		seekbarBroadcastIntent = new Intent(ACTION_BROADCAST_SEEKBAR);
		repeatBroadcastIntent = new Intent(ACTION_BROADCAST_REPEAT);
		shuffleBroadcastIntent = new Intent(ACTION_BROADCAST_SHUFFLE);
		
		registerReceiver(broadcastTrackDataReceiver, new IntentFilter(MusicService.ACTION_BROADCAST_TRACK_DATA));
		registerSeekBroadcastReceiver();
		
		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this);
		
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.i(TAG, "button play clicked");
				startMusicAction(MusicService.ACTION_TOGGLE_PLAYBACK);
				if (isPlaying) {
					isPlaying = false;
					//unregisterSeekBroadcastReceiver();
					btnPlay.setImageResource(R.drawable.btn_play);
				} else {
					isPlaying = true;
					//registerSeekBroadcastReceiver();
					btnPlay.setImageResource(R.drawable.btn_pause);
				}
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
				Intent i = new Intent(getApplicationContext(), ArtistsListActivity.class);
				startActivityForResult(i, 100);			
			}
		});
		
    	Intent intent = new Intent(MusicService.ACTION_CHANGE_PLAYLIST);
    	Log.i(TAG, getIntent().getExtras().toString());
    	Log.i(TAG, getIntent().getExtras().getString(MediaProvider.FILTER));
    	intent.putExtras(getIntent().getExtras());
    	startService(intent);
		
	}

	private BroadcastReceiver broadcastTrackDataReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "TrackData onReceive()");
			String title = intent.getStringExtra(MusicService.EXTRA_TITLE);
			int duration = intent.getIntExtra(MusicService.EXTRA_DURATION, 0);
			songTitleLabel.setText(title);
			songProgressBar.setMax(duration);
			songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(duration));
			
			btnPlay.setImageResource(R.drawable.btn_pause);
		}
		
	};

	private BroadcastReceiver broadcastSeekReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Seek onReceive()");
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
}