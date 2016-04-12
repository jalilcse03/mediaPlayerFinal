package bubtjobs.com.audioplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class PlayListActivity extends ListActivity {
	private ArrayList<Song> songList;
	ListView sampleLV;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		songList=new ArrayList<>();
		getSongList();
		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});



		if(songList!=null)
		{
			SongAdapter adapter=new SongAdapter(getApplicationContext(),songList);
			setListAdapter(adapter);
			ListView lv = getListView();

			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
					// getting listitem index
					int songIndex = position;
					//Toast.makeText(PlayListActivity.this, ""+songIndex, Toast.LENGTH_SHORT).show();
					// Starting new intent
					Intent in = new Intent(getApplicationContext(),
							HomeActivity.class);
					// Sending songIndex to PlayerActivity
					in.putExtra("songIndex", songIndex);
					setResult(100, in);
					// Closing PlayListView
					finish();
				}
			});


		}
		else{
			Toast.makeText(PlayListActivity.this, "No song", Toast.LENGTH_SHORT).show();
		}

	}

	public void getSongList() {
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

		if(musicCursor!=null && musicCursor.moveToFirst()){
			//get columns
			int titleColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media.ARTIST);
			//add songs to list
			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist));
			}
			while (musicCursor.moveToNext());
		}
	}

}
