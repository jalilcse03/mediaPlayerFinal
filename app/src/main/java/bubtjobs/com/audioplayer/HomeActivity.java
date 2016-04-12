package bubtjobs.com.audioplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    ImageButton songListBt,shuffleBt,repeatBt,preBt,playBt,nextBt;
    ImageView background;
    TextView songStartTv,songEndTv,songTitleTv;
    private SeekBar songProgressBar;

    private ArrayList<Song> songList;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private Handler mHandler = new Handler();
    private Utilities utils;
    boolean isFirstActivity=true,sessionData=false,seseionShuffle=false,sessionRepeat=false;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    String title="";
    int postion=0;
    SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    @Override
    protected void onDestroy() {
        sessionManager.setId(String.valueOf(musicSrv.reSongPos()),isShuffle,isRepeat);
        stopService(playIntent);
        musicSrv=null;
        mHandler.removeCallbacks(mUpdateTimeTask);
        super.onDestroy();
    }
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void init() {
        utils = new Utilities();
        sessionManager=new SessionManager(this);
        isFirstActivity=true;sessionData=false;seseionShuffle=false;sessionRepeat=false;


        songListBt=(ImageButton)findViewById(R.id.songListBt);
        shuffleBt=(ImageButton)findViewById(R.id.shuffleBt);
        repeatBt=(ImageButton)findViewById(R.id.repeatBt);
        preBt=(ImageButton)findViewById(R.id.preBt);
        playBt=(ImageButton)findViewById(R.id.playBt);
        nextBt=(ImageButton)findViewById(R.id.nextBt);
        background=(ImageView)findViewById(R.id.bk);

        songStartTv=(TextView)findViewById(R.id.songStartTv);
        songEndTv=(TextView)findViewById(R.id.songEndTv);
        songTitleTv=(TextView)findViewById(R.id.songTitleTv);

        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);

        songListBt.setOnClickListener(this);
        shuffleBt.setOnClickListener(this);
        repeatBt.setOnClickListener(this);
        preBt.setOnClickListener(this);
        playBt.setOnClickListener(this);
        nextBt.setOnClickListener(this);
        songProgressBar.setOnSeekBarChangeListener(this);
        songList = new ArrayList<Song>();
        getSongList();

        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        // initial state
        if(sessionManager.getId()!=null)
        {
            sessionData=true;
            isFirstActivity=false;
            postion=Integer.parseInt(sessionManager.getId());
            songTitleTv.setText(songList.get(postion).getTitle());

            if(sessionManager.getRepeat())
            {
                //musicSrv.setRepeat();
                sessionRepeat=true;
                isRepeat = true;
                isShuffle = false;
                repeatBt.setImageResource(R.drawable.repeat_on);
            }
            else if(sessionManager.getShuffle())
            {
                //musicSrv.setSuffle();
                seseionShuffle=true;
                isShuffle = true;
                isRepeat = false;

                shuffleBt.setImageResource(R.drawable.shuffle_on);
            }

        }
        else{
            songTitleTv.setText(songList.get(0).getTitle());
        }
        // initial end


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

    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 1000);
    }
    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = musicSrv.getDur();
            long currentDuration = musicSrv.getPosn();

            // Displaying Total Duration time
            songEndTv.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songStartTv.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);
            if(title.equals(musicSrv.getSongTitle()))
            {

            }
            else{
                songTitleTv.setText(musicSrv.getSongTitle());
            }
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 1000);

        }
    };


    @Override
    public void onClick(View v) {

        // session update
        if(seseionShuffle)
        {
            musicSrv.setSuffle();
            seseionShuffle=false;
        }
        if(sessionRepeat)
        {
            musicSrv.setRepeat();
            sessionRepeat=false;
        }

        if(v.getId()==R.id.songListBt)
        {
           // Toast.makeText(HomeActivity.this, "SongList", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
            startActivityForResult(i, 100);

        }
        else if(v.getId()==R.id.shuffleBt)
        {
           // Toast.makeText(HomeActivity.this, "Shuffle", Toast.LENGTH_SHORT).show();
            if(isShuffle) {
                musicSrv.setSuffle();
                isShuffle = false;
                Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                shuffleBt.setImageResource(R.drawable.suffel_off);
            }
            else {
                musicSrv.setSuffle();
                isShuffle = true;
                Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                // make shuffle to false
                isRepeat = false;

                shuffleBt.setImageResource(R.drawable.shuffle_on);
                repeatBt.setImageResource(R.drawable.repeat_off);
            }
        }
        else if(v.getId()==R.id.repeatBt)
        {
           // Toast.makeText(HomeActivity.this, "Repeat", Toast.LENGTH_SHORT).show();

            if(isRepeat) {
                musicSrv.setRepeat();
                isRepeat = false;

                Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                repeatBt.setImageResource(R.drawable.repeat_off);
            }
            else{
                musicSrv.setRepeat();
                isRepeat = true;
                Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                // make shuffle to false
                isShuffle = false;

                repeatBt.setImageResource(R.drawable.repeat_on);
                shuffleBt.setImageResource(R.drawable.suffel_off);
            }

        }
        else if(v.getId()==R.id.preBt)
        {
            //Toast.makeText(HomeActivity.this, "Pre", Toast.LENGTH_SHORT).show();

            musicSrv.playPrev();
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            updateProgressBar();
        }
        else if(v.getId()==R.id.playBt)
        {

            if(isFirstActivity && musicSrv.isPng()==false) {
                musicSrv.setSong(0);
                musicSrv.playSong();
                isFirstActivity = false;
                songProgressBar.setProgress(0);
                songProgressBar.setMax(100);
                updateProgressBar();
                playBt.setImageResource(R.drawable.pause);
            }
            else if(sessionData)
            {
                musicSrv.setSong(postion);
                musicSrv.playSong();
                sessionData=false;
                songProgressBar.setProgress(0);
                songProgressBar.setMax(100);
                updateProgressBar();
                playBt.setImageResource(R.drawable.pause);
            }
            else if(musicSrv.isPng())
            {
                musicSrv.pausePlayer();
                playBt.setImageResource(R.drawable.play);
            }
            else
            {
                musicSrv.go();
                updateProgressBar();
                playBt.setImageResource(R.drawable.pause);
            }

        }
        else if(v.getId()==R.id.nextBt)
        {
            musicSrv.playNext();
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            updateProgressBar();

            //Toast.makeText(HomeActivity.this, "next", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = musicSrv.getDur();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        musicSrv.seek(currentPosition);

        // update timer progress again
        updateProgressBar();
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            int currentSongIndex = data.getExtras().getInt("songIndex");
            musicSrv.setSong(currentSongIndex);
            musicSrv.playSong();
            isFirstActivity = false;
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            updateProgressBar();
            playBt.setImageResource(R.drawable.pause);

        }

    }
}
