package bubtjobs.com.audioplayer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Murtuza on 4/9/2016.
 */
public class SongAdapter extends ArrayAdapter<Song> {
    private ArrayList<Song>personModelArrayList;
    private Context context;
    public SongAdapter(Context context, ArrayList<Song> personModelArrayList) {
        super(context, R.layout.playlist_item,personModelArrayList);
        this.context=context;
        this.personModelArrayList=personModelArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=convertView;
        LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view=layoutInflater.inflate(R.layout.playlist_item,null);
        TextView nameTV= (TextView) view.findViewById(R.id.songTitle);
        nameTV.setText(personModelArrayList.get(position).getTitle());
        return view;
    }
}