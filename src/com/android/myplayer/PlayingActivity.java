package com.android.myplayer;

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

import com.androidhive.musicplayer.R;
import com.androidhive.musicplayer.R.id;

public class PlayingActivity extends Activity implements OnSeekBarChangeListener {

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
	
	private Utilities utils;
	
	private MusicService service;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		
		setMusicService();
		
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
		
		utils = new Utilities();
		
		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this);
		
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.i(TAG, "button play clicked");
				service.processTogglePlaybackRequest();
				if (isPlaying) {
					isPlaying = false;
					btnPlay.setImageResource(R.drawable.btn_play);
				} else {
					isPlaying = true;
					btnPlay.setImageResource(R.drawable.btn_pause);
				}
			}
		});
		
		btnForward.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				service.processForwardRequest();
			}
		});
		
		btnBackward.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				service.processBackwardRequest();
			}
		});
		
		btnNext.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				service.processSkipRequest();
			}
		});
		
		btnPrevious.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				service.processRewindRequest();
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
				service.setRepeat(isRepeat);
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
				service.setShuffle(isShuffle);
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
		
		Log.i(TAG, getIntent().getExtras().toString());
    	Log.i(TAG, getIntent().getExtras().getString(MediaProvider.FILTER));
    	
    	service.processChangePlaylistRequest(getIntent().getExtras());
	}
	
	private void setMusicService() {
		startService(new Intent(this, MusicService.class));
		try { Thread.sleep(1000); } catch(InterruptedException e) {}
	}

	private class TrackDataReceiver {
		public void onReceive(Bundle extras) {
			Log.i(TAG, "TrackData onReceive()");
			String title = extras.getString(MusicService.EXTRA_TITLE);
			int duration = extras.getInt(MusicService.EXTRA_DURATION, 0);
			songTitleLabel.setText(title);
			songProgressBar.setMax(duration);
			songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(duration));
			
			btnPlay.setImageResource(R.drawable.btn_pause);
		}
	}
	
	/*
	private BroadcastReceiver broadcastSeekReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.i(TAG, "Seek onReceive()");
			int currentPosition = intent.getIntExtra(MusicService.EXTRA_CURRENT_POSITION, 0);
			songProgressBar.setProgress(currentPosition);
			songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentPosition));
		}
		
	};
	*/
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
    }

	@Override
    public void onStopTrackingTouch(SeekBar seekBar) {
		int seekPos = seekBar.getProgress();
		service.updateSeekPos(seekPos);
    }
	
	@Override
	public void onStop() {
		super.onStop();
	}
}