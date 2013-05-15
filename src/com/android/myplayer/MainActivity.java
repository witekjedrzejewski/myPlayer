package com.android.myplayer;

import list.AlbumsListActivity;
import list.ArtistsListActivity;
import list.SongsListActivity;

import com.androidhive.musicplayer.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class MainActivity extends TabActivity {

	private static final String tag = "MainActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(tag, "onCreate");
		
		super.onCreate(savedInstanceState);
		Log.d(tag, "done with super");
		
		setContentView(R.layout.activity_main);
		Log.d(tag, "set layout");
		
		Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Reusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, ArtistsListActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("artists").setIndicator("Artists",
	                      res.getDrawable(R.drawable.ic_tab_artists))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, AlbumsListActivity.class);
	    spec = tabHost.newTabSpec("albums").setIndicator("Albums",
	                      res.getDrawable(R.drawable.ic_tab_albums))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, SongsListActivity.class);
	    spec = tabHost.newTabSpec("songs").setIndicator("Songs",
	                      res.getDrawable(R.drawable.ic_tab_songs))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}

}
