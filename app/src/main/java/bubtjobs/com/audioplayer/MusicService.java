package bubtjobs.com.audioplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;

import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

/**
 * Created by Murtuza on 4/9/2016.
 */
public class MusicService  extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnBufferingUpdateListener {

    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();

    private String songTitle="ok";
    private static final int NOTIFY_ID=1;
    private boolean shuffle=false,repeat=false;
    private Random rand;
    private int mBufferPosition;

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize position
        songPosn=0;
        //create player
        rand=new Random();
        player = new MediaPlayer();
        initMusicPlayer();
    }
    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }


    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    public int reSongPos(){
       return songPosn;
    }

    public void playSong(){
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        songTitle=playSong.getTitle();
//get id
        long currSong = playSong.getID();
//set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }




    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        setBufferPosition(percent * getDur() / 100);
    }

    protected void setBufferPosition(int progress) {
        mBufferPosition = progress;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

       player.start();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // player.seekTo(0);
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, HomeActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.app_icon)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    public int getBufferPercentage() {
        return mBufferPosition;
    }
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }




    public void pausePlayer() {
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }
    public void playPrev(){
        if(repeat)
        {
            playSong();
        }
        else {
            if(shuffle)
            {

                int newSong = songPosn;
                while(newSong==songPosn){
                    newSong=rand.nextInt(songs.size());
                }
                songPosn=newSong;
                playSong();
            }
            else {
                songPosn--;
                if (songPosn < 0) songPosn = songs.size() - 1;
                playSong();
            }
        }
    }

    //skip to next
    public void playNext() {
        if (repeat) {
            playSong();
        }
        else {

        if(shuffle){
            songPosn++;
            if (songPosn > songs.size() - 1) songPosn = 0;
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
            playSong();
        }
        else{
            songPosn++;
            if(songPosn==songs.size()) songPosn=0;
            playSong();
        }

        }
    }


    public void setSuffle(){
        if(shuffle) shuffle=false;
        else {
            shuffle = true;
            repeat=false;
        }
    }
    public void setRepeat(){
        if(repeat) repeat=false;
        else {
            repeat = true;
            shuffle=false;
        }

    }


    public String getSongTitle(){
        return songTitle;
    }


    public void seekupdate(){

    }
}