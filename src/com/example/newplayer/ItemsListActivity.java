package com.example.newplayer;


import com.example.newplayer.MediaProvider;
import com.example.newplayer.R;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


abstract public class ItemsListActivity extends ListActivity {
	
	protected MediaProvider provider;
	
	abstract protected Cursor getCursor();
	abstract protected Class<?> getNextActivity();
	abstract protected String[] getFrom();
	abstract protected void addNextExtras(Intent intent);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);

		provider = new MediaProvider(getContentResolver());
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this, R.layout.playlist_item, getCursor(), 
				getFrom(), new int[]{R.id.itemTitle});

		setListAdapter(adapter);

		ListView lv = getListView();
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent intent = new Intent(getApplicationContext(),
						getNextActivity());

				intent.putExtra("item", id);
				intent.putExtra("position", position);
				
				addNextExtras(intent);

				startActivityForResult(intent, 100);
				
			}
		});

	}

}
