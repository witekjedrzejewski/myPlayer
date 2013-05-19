
	/*
	public Cursor getArtists() {
		Uri uri = Audio.Artists.EXTERNAL_CONTENT_URI;
		return contentResolver.query(uri, null, null, null, null);
		Log.d("getArtists", "cursor");
		if (cursor.moveToFirst()) {
			Log.d("getArtists", "cursor not empty");
			int nameColumn = cursor.getColumnIndex(Audio.Artists.ARTIST);
			int idColumn = cursor.getColumnIndex(Audio.Artists._ID);
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("itemName", cursor.getString(nameColumn));
				map.put("artistId", Integer.toString(cursor.getInt(idColumn)));
				list.add(map);
			} while (cursor.moveToNext());
		}
		return list;
	}*/

package com.android.myplayer;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.util.Log;

public class MediaProvider {
	
	public static final String FILTER = "mediaProviderFilter";
	public static final String ARG = "mediaProviderArg";
	public static final String ALL_FILTER = "mediaProviderAllFilter";
	public static final String ARTIST_FILTER = "mediaProviderArtistFilter";
	public static final String ALBUM_FILTER = "mediaProviderAlbumFilter";
	
	private ContentResolver contentResolver;
	private static final String TAG = "MediaProvider";

	public class Song {

		private long id;
		private String title;
		
		public Song(Cursor cursor) {
			int titleColumn = cursor.getColumnIndex(Audio.Media.TITLE);
			int idColumn = cursor.getColumnIndex(Audio.Media._ID);
			title = cursor.getString(titleColumn);
			id = cursor.getLong(idColumn);
		}
		
		public Uri getURI() {
			return ContentUris.withAppendedId(Audio.Media.EXTERNAL_CONTENT_URI, id);
		}

		public String getTitle() {
			return title;
		}

	}
	
	// Constructor
	public MediaProvider(ContentResolver contentResolver){
		this.contentResolver = contentResolver;
	}
	
	public ArrayList<Song> getSongs(String filter, Long arg) {
		Log.i(TAG, filter + " " + arg);
		if (filter.equals(ALL_FILTER)) return getAllSongs();
		if (filter.equals(ARTIST_FILTER)) return getSongsFromArtist(arg);
		if (filter.equals(ALBUM_FILTER)) return getSongsFromAlbum(arg);
		return null;
	}

	public Cursor getAllArtistsCursor() {
		Uri uri = Audio.Artists.EXTERNAL_CONTENT_URI;
		return contentResolver.query(uri, null, null, null, null);
	}

	public Cursor getAllAlbumsCursor() {
		Uri uri = Audio.Albums.EXTERNAL_CONTENT_URI;
		return contentResolver.query(uri, null, null, null, null);
	}
	
	public Cursor getAllSongsCursor() {
		Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;
		return contentResolver.query(uri, null, null, null, null);
	}
	
	public Cursor getAlbumsFromArtistCursor(long artistId) {
		Uri uri = Audio.Artists.Albums.getContentUri("external", artistId);
		return contentResolver.query(uri, null, null, null, null);
	}
	
	public Cursor getSongsFromAlbumCursor(long albumId) {
		Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;
		String where = Audio.Media.ALBUM_ID + "=?";
		String[] whereValues = {Long.toString(albumId)};
		return contentResolver.query(uri, null, where, whereValues, null);
	}
	
	public Cursor getSongsFromArtistCursor(long artistId) {
		Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;
		String where = Audio.Media.ARTIST_ID + "=?";
		String[] whereValues = {Long.toString(artistId)};
		return contentResolver.query(uri, null, where, whereValues, null);
	}
	
	private ArrayList<Song> listFromCursor(Cursor cursor) {
		ArrayList<Song> list = new ArrayList<Song>();
		if (cursor.moveToFirst()) {
			do {
				list.add(new Song(cursor));
			} while (cursor.moveToNext());
		}
		return list;
	}
	
	private ArrayList<Song> getAllSongs() {
		return listFromCursor(getAllSongsCursor());
	}
	
	private ArrayList<Song> getSongsFromAlbum(long albumId) {
		Log.i(TAG, "getSongsFromAlbum");
		return listFromCursor(getSongsFromAlbumCursor(albumId));
	}

	private ArrayList<Song> getSongsFromArtist(long artistId) {
		return listFromCursor(getSongsFromArtistCursor(artistId));
	}
}
