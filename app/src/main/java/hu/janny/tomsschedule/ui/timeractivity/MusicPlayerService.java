package hu.janny.tomsschedule.ui.timeractivity;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * This is the service what provides music playing in the timer activity.
 */
public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener{

    public final static String MUSIC_RESOURCE = "music_resource";
    private int musicResource;

    MediaPlayer musicPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra(MUSIC_RESOURCE)) {
            musicResource = intent.getIntExtra(MUSIC_RESOURCE,0);
        }
        musicPlayer = MediaPlayer.create(getApplicationContext(), musicResource);
        musicPlayer.setLooping(true);
        musicPlayer.start();
        return super.onStartCommand(intent, flags, startId);
        //return START_REDELIVER_INTENT;
    }

    public void onPrepared(MediaPlayer player) {
        musicPlayer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicPlayer != null) {
            musicPlayer.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.release();
        return false;
    }
}
