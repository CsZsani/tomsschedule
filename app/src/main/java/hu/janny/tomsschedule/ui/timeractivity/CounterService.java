package hu.janny.tomsschedule.ui.timeractivity;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CounterService extends Service {

    private Intent intent;
    public static final String BROADCAST_ACTION = "hu.janny.tomsschedule.Timer";

    private Handler handler = new Handler();
    private long initial_time;
    long timeInMilliseconds = 0L;

    public CounterService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        initial_time = SystemClock.uptimeMillis();
        intent = new Intent();
        intent.setAction(BROADCAST_ACTION);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            DisplayLoggingInfo();
            handler.postDelayed(this, 1000); // 1 seconds
        }
    };

    private void DisplayLoggingInfo() {

        timeInMilliseconds = SystemClock.uptimeMillis() - initial_time;

        intent.putExtra("time", timeInMilliseconds);
        sendBroadcast(intent);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(sendUpdatesToUI);
        System.out.println("ondestroy counter");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
