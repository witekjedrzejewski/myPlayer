/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.newplayer;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.example.newplayer.R;

/**
 * Service that handles media playback. This is the Service through which we perform all the media
 * handling in our application. Upon initialization, it starts a {@link MusicRetriever} to scan
 * the user's media. Then, it waits for Intents (which come from our main activity,
 * {@link MainActivity}, which signal the service to perform specific operations: Play, Pause,
 * Rewind, Skip, etc.
 */
public class MusicService extends Service implements OnCompletionListener, OnPreparedListener,
                OnErrorListener, MusicFocusable {

    // The tag we put on debug messages
    final static String TAG = "MusicService";

    public static final String ACTION_CHANGE_PLAYLIST = "com.example.newplayer.action.CHANGE_PLAYLIST";
    public static final String ACTION_TOGGLE_PLAYBACK = "com.example.newplayer.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.example.newplayer.action.PLAY";
    public static final String ACTION_PAUSE = "com.example.newplayer.action.PAUSE";
    public static final String ACTION_STOP = "com.example.newplayer.action.STOP";
    public static final String ACTION_SKIP = "com.example.newplayer.action.SKIP";
    public static final String ACTION_PREVIOUS = "com.example.newplayer.action.PREVIOUS";
    public static final String ACTION_FORWARD = "com.example.newplayer.action.FORWARD";
    public static final String ACTION_BACKWARD = "com.example.newplayer.action.BACKWARD";

    public static final String EXTRA_TITLE = "com.example.newplayer.MusicService.title";
    public static final String EXTRA_DURATION = "com.example.newplayer.MusicService.duration";
    public static final String EXTRA_CURRENT_POSITION = "com.example.newplayer.MusicService.current_position";
    public static final String EXTRA_PLAYING = "com.example.newplayer.MusicService.playing";
    public static final String EXTRA_POS = "com.example.newplayer.MusicService.pos";
    
    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;

    private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds
	
	private int previousLimit = 3000; //when to go to previous song
    // our media player
    MediaPlayer mPlayer = null;

    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;

    // indicates the state our service:
    enum State {
        Stopped,    // media player is stopped and not prepared to play
        Preparing,
        Playing,    // playback active (media player ready!). (but the media player may actually be
                    // paused in this state if we don't have audio focus. But we stay in this state
                    // so that we know we have to resume playback once we get focus back)
        Paused,      // playback paused (media player ready!)
    };

    State mState = State.Stopped;

    enum PauseReason {
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    };

    // why did we pause? (only relevant if mState == State.Paused)
    PauseReason mPauseReason = PauseReason.UserRequest;

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    // title of the song we are currently playing
    String mSongTitle = "";

    // whether the song we are playing is streaming from the network
    boolean mIsStreaming = false;

    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    final int NOTIFICATION_ID = 1;

    Playlist playlist;
    
    // Dummy album art we will pass to the remote control (if the APIs are available).
    Bitmap mDummyAlbumArt;

    // The component name of MusicIntentReceiver, for use with media button and remote control
    // APIs
    ComponentName mMediaButtonReceiverComponent;

    AudioManager mAudioManager;
    NotificationManager mNotificationManager;

    Notification mNotification = null;
    
    private final Handler handler = new Handler();
    private Intent seekIntent;
    private Intent trackDataIntent;
    public static final String ACTION_BROADCAST_SEEK = "com.example.newplayer.action.BROADCAST_SEEK";
    public static final String ACTION_BROADCAST_TRACK_DATA = "com.example.newplayer.action.BROADCAST_TRACK_DATA";
    
    private boolean isSeekPosBroadcastReceiverRegistered = false;
    private boolean isRepeatBroadcastReceiverRegistered = false;
    private boolean isShuffleBroadcastReceiverRegistered = false;
    private boolean isFlagBroadcastReceiverRegistered = false;
    
    private void assertRegistered(BroadcastReceiver r, String action, boolean isRegistered) {
    	if (!isRegistered) {
    		registerReceiver(r, new IntentFilter(action));
    		isRegistered = true;
    	}
    }
    
    private void assertUnregistered(BroadcastReceiver r, boolean isRegistered) {
    	if (isRegistered) {
    		unregisterReceiver(r);
    		isRegistered = false;
    	}
    }
    
    private void setupHandler() {
    	Log.i("SETUP HANDLER", "go");
    	handler.removeCallbacks(sendUpdatesToUI);
    	handler.post(sendUpdatesToUI);
    }
    
    private Runnable sendUpdatesToUI = new Runnable() {
    	public void run() {
    		sendUpdateToUI();
    		handler.postDelayed(this, 1000);
    	}
    };

    private void sendUpdateToUI() {
    	seekIntent.putExtra(EXTRA_CURRENT_POSITION, mPlayer.getCurrentPosition());
    	//Log.i(TAG, "sending seek broadcast");
		sendBroadcast(seekIntent);
    }
 
    /**
     * Makes sure the media player exists and has been reset. This will create the media player
     * if needed, or reset the existing media player if one already exists.
     */
    void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while playing. If we don't do
            // that, the CPU might go to sleep while the song is playing, causing playback to stop.
            //
            // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing, and when it's done
            // playing:
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        }
        else
            mPlayer.reset();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.i(TAG, "onStartCommand");
    	
      	assertRegistered(seekPosBroadcastReceiver, PlayingActivity.ACTION_BROADCAST_SEEKBAR,
    			isSeekPosBroadcastReceiverRegistered);
    	assertRegistered(repeatBroadcastReceiver, PlayingActivity.ACTION_BROADCAST_REPEAT,
    			isRepeatBroadcastReceiverRegistered);
    	assertRegistered(shuffleBroadcastReceiver, PlayingActivity.ACTION_BROADCAST_SHUFFLE,
    			isShuffleBroadcastReceiverRegistered);
    	assertRegistered(flagBroadcastReceiver, PlayingActivity.ACTION_BROADCAST_FLAG,
    			isFlagBroadcastReceiverRegistered);
    	
    	
    	String action = intent.getAction();
    	if (action.equals(ACTION_CHANGE_PLAYLIST)) processChangePlaylistRequest(intent.getExtras());
    	else if (action.equals(ACTION_TOGGLE_PLAYBACK)) processTogglePlaybackRequest();
        else if (action.equals(ACTION_PLAY)) processPlayRequest();
        else if (action.equals(ACTION_PAUSE)) processPauseRequest();
        else if (action.equals(ACTION_SKIP)) processSkipRequest();
        else if (action.equals(ACTION_STOP)) processStopRequest();
        else if (action.equals(ACTION_PREVIOUS)) processRewindRequest();
        else if (action.equals(ACTION_FORWARD)) processForwardRequest();
        else if (action.equals(ACTION_BACKWARD)) processBackwardRequest();
        else if (action.equals(ACTION_BROADCAST_TRACK_DATA)) sendTrackData();

    	return START_NOT_STICKY;
    }
    
    @Override
    public void onCreate() {
    	super.onCreate();
        Log.i(TAG, "debug: Creating service");
        
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        seekIntent = new Intent(ACTION_BROADCAST_SEEK);
        trackDataIntent = new Intent(ACTION_BROADCAST_TRACK_DATA);

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus

        mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.dummy_album_art);
    }

    void processChangePlaylistRequest(Bundle extras) {
    	Log.i(TAG, extras.getString(MediaProvider.FILTER));
    	if (playlist != null) {
    		playlist.sendUpdateToDB(); //updating ratings
    	}
    	playlist = new Playlist(getApplicationContext(), getContentResolver(), extras);
    	Log.i(TAG, "playlist for service created");
    	mState = State.Stopped;
        processPlayRequest();
    }
    
    void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }
    }

    void processPlayRequest() {
    	Log.i(TAG, "processPlayRequest()");
        tryToGetAudioFocus();

        Log.i(TAG, "audio focus gained");
        // actually play the song

        setupHandler();
        Log.i(TAG, "Handler set up");
    	
        if (mState == State.Stopped) {
            // If we're stopped, just go ahead to the next song and start playing
            playNextSong(false); //pretty natural transition
        }
        else if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.Playing;
            setUpAsForeground(mSongTitle + " (playing)");
            configAndStartMediaPlayer();
        }
    }

    void processPauseRequest() {
        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus
        }
    }

    void processRewindRequest() {
        if (mState == State.Playing || mState == State.Paused) {
        	if (mPlayer.getCurrentPosition() >= previousLimit) {
        		updateSeekPos(0);
        	} else {
        		tryToGetAudioFocus();
        		playPreviousSong();
        	}
        }
    }

    void processSkipRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            playNextSong(true);
        }
    }

    void processForwardRequest() {
    	if (mState == State.Playing || mState == State.Paused) {
    		int forwardedPosition = mPlayer.getCurrentPosition() + seekForwardTime;
    		updateSeekPos(Math.min(mPlayer.getDuration(), forwardedPosition));
    	}
    }

    void processBackwardRequest() {
    	if (mState == State.Playing || mState == State.Paused) {
    		int backwardedPosition = mPlayer.getCurrentPosition() - seekBackwardTime;
    		updateSeekPos(Math.max(0, backwardedPosition));
    	}
    }

    void processStopRequest() {
        processStopRequest(false);
    }

    void processStopRequest(boolean force) {
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;

            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();

            // service is no longer necessary. Will be started again if needed.
            stopSelf();
        }
    }
    
    private BroadcastReceiver flagBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int flag = intent.getIntExtra(PlayingActivity.EXTRA_FLAG, 0);
			Log.i(TAG, "received flag = " + flag);
			playlist.setFlag(flag);
		}
	};

    private BroadcastReceiver repeatBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			playlist.setRepeat(intent.getBooleanExtra(PlayingActivity.EXTRA_IS_REPEAT, false));
		}
	};

	private BroadcastReceiver shuffleBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			playlist.setShuffle(intent.getBooleanExtra(PlayingActivity.EXTRA_IS_SHUFFLE, false));
		}
	};

	// --Receive seekbar position if it has been changed by the user in the
	// activity
	private BroadcastReceiver seekPosBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateSeekPos(intent.getIntExtra(PlayingActivity.EXTRA_SEEKPOS, 0));
		}
	};

	// Update seek position
	private void updateSeekPos(int position) {
		mPlayer.seekTo(position);
		Log.i(TAG, "seeked");
		sendUpdateToUI();
	}
	/**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        stopForeground(true);
        
        handler.removeCallbacks(sendUpdatesToUI);
        
        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying()) mPlayer.pause();
            return;
        }
        else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud

        if (!mPlayer.isPlaying()) mPlayer.start();
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                        && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    void playNextSong(boolean forceSkipped) {
    	playSong(true, forceSkipped);
    }
    
    void playPreviousSong() {
    	playSong(false, true);
    }
    
    /**
     * Starts playing the next/previous song
     */
    void playSong(boolean next, boolean forceSkipped) {
        mState = State.Stopped;
        relaxResources(false); // release everything except MediaPlayer

        try {
        	MediaProvider.Song song = (next) ? 
        			playlist.getNextSong(forceSkipped) : playlist.getPreviousSong();
	        Log.i(TAG, song.getTitle() + " " + song.getURI().toString());
	        
	        createMediaPlayerIfNeeded();
	        Log.i(TAG, "media player created");
	        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        mPlayer.setDataSource(getApplicationContext(), song.getURI());
	        
	        mSongTitle = song.getTitle();
	        mState = State.Preparing;
	        setUpAsForeground(mSongTitle + " (loading)");
	       
	        mPlayer.prepareAsync();
        } catch (IOException ex) {
        	Log.e("MusicService", "IOException playing next song: " + ex.getMessage());
        }
    }

    /** Called when media player is done playing current song. */
    public void onCompletion(MediaPlayer player) {
        // The media player finished playing the current song
        playNextSong(false); //natural transition
    }

    private void sendTrackData() {
    	if (mState == State.Playing || mState == State.Paused) {
	    	trackDataIntent.putExtra(EXTRA_TITLE, mSongTitle);
	        trackDataIntent.putExtra(EXTRA_DURATION, mPlayer.getDuration());
	        trackDataIntent.putExtra(EXTRA_PLAYING, mState == State.Playing);
	        trackDataIntent.putExtra(EXTRA_POS, mPlayer.getCurrentPosition());
	        sendBroadcast(trackDataIntent);
    	}
    }
    
    /** Called when media player is done preparing. */
    public void onPrepared(MediaPlayer player) {
        // The media player is done preparing. That means we can start playing!
        mState = State.Playing;
        updateNotification(mSongTitle + " (playing)");
        
        sendTrackData();
        setupHandler();

        configAndStartMediaPlayer();
    }

    /** Updates the notification. */
    void updateNotification(String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.setLatestEventInfo(getApplicationContext(), "Smart Player", text, pi);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing
     * something the user is actively aware of (such as playing music), and must appear to the
     * user as a notification. That's why we create the notification here.
     */
    void setUpAsForeground(String text) {
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification = new Notification();
        mNotification.tickerText = text;
        mNotification.icon = R.drawable.ic_stat_playing;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNotification.setLatestEventInfo(getApplicationContext(), "MyMusicPlayer",
                text, pi);
        startForeground(NOTIFICATION_ID, mNotification);
    }

    /**
     * Called when there's an error playing media. When this happens, the media player goes to
     * the Error state. We warn the user about the error and reset the media player.
     */
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
            Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        return true; // true indicates we handled the error
    }

    public void onGainedAudioFocus() {
        Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;

        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer();
    }

    public void onLostAudioFocus(boolean canDuck) {
        Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
            "no duck"), Toast.LENGTH_SHORT).show();
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();
    }

    @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        mState = State.Stopped;
        if (playlist != null) {
        	playlist.sendUpdateToDB(); //update counted ratings
        }
        relaxResources(true);
        giveUpAudioFocus();
        handler.removeCallbacks(sendUpdatesToUI);
        assertUnregistered(seekPosBroadcastReceiver, isSeekPosBroadcastReceiverRegistered);
        assertUnregistered(repeatBroadcastReceiver, isRepeatBroadcastReceiverRegistered);
        assertUnregistered(shuffleBroadcastReceiver, isShuffleBroadcastReceiverRegistered);
        assertUnregistered(flagBroadcastReceiver, isFlagBroadcastReceiverRegistered);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public boolean isPlaying() {
    	return mPlayer.isPlaying();
    }
}
