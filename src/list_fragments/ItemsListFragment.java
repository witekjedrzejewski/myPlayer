package list_fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.android.myplayer.MediaProvider;
import com.androidhive.musicplayer.R;


abstract public class ItemsListFragment extends ListFragment {
	
	protected MediaProvider provider;
	
	abstract protected Cursor getCursor();
	abstract protected Class<?> getNextActivity();
	abstract protected String[] getFrom();
	abstract protected void addNextExtras(Intent intent);
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		provider = new MediaProvider(getActivity().getContentResolver());
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				getActivity(), R.layout.playlist_item, getCursor(), 
				getFrom(), new int[]{R.id.itemTitle});

		setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View view, int position, long id) {
				
		Intent intent = new Intent(getActivity().getApplicationContext(),
				getNextActivity());

		intent.putExtra("item", id);
		intent.putExtra("position", position);
		
		addNextExtras(intent);
		//startActivity(intent);
		startActivityForResult(intent, 100);
		
	}

}
