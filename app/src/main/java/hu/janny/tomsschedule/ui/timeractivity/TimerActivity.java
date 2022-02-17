package hu.janny.tomsschedule.ui.timeractivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.ActivityTimerBinding;
import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.model.TimerAssets;
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.ui.main.MainViewModel;

public class TimerActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private ActivityTimerBinding binding;
    private long customActivityId;
    private String customActivityName;
    public final static String ACTIVITY_ID = "activity_id";
    public final static String ACTIVITY_NAME = "activity_name";
    private final String TIMER_INITIAL_MILLIS = "timer_initial_millis";
    private final String ASSET_NUM = "asset_num";
    private final String MUSIC_ON = "music_on";
    private long currentTime = 0L;
    private MainViewModel mainViewModel;
    private User currentUser;
    private int currentAsset;
    private boolean musicOn;
    private int maxAssetNum;
    ActionBar actionBar;

    private Intent counterIntent;
    private Intent musicIntent;
    private NotificationManager notificationManager;
    private long todayMillis;
    private long initialMillis;

    private Handler handler = new Handler();

    public static boolean started = false;
    private CustomActivity customActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding = ActivityTimerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        actionBar = getSupportActionBar();

        maxAssetNum = TimerAssets.maxAssetNum();
        sharedPref = getApplicationContext().getSharedPreferences(
                "hu.janny.tomsschedule.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        currentAsset = sharedPref.getInt(ASSET_NUM, 0);
        musicOn = sharedPref.getBoolean(MUSIC_ON, true);
        initialMillis = sharedPref.getLong(TIMER_INITIAL_MILLIS, 0L);
        setUIAsset();
        if (initialMillis == 0L) {
            initialMillis = SystemClock.uptimeMillis();
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(TIMER_INITIAL_MILLIS, initialMillis);
        editor.apply();

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        customActivityId = extras.getLong(ACTIVITY_ID);
        customActivityName = extras.getString(ACTIVITY_NAME);
        mainViewModel.findActivityById(customActivityId);

        mainViewModel.getSingleActivity().observe(this, new Observer<CustomActivity>() {
            @Override
            public void onChanged(CustomActivity activity) {
                if (activity != null) {
                    customActivity = activity;
                } else {
                    Toast.makeText(TimerActivity.this, "I can't find this activity! Something went wrong :(", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(TimerActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        //startCounterService();
        //initMusic();

        started = true;

        mainViewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
            }
        });

        initTodayInMillis();

        initStopButton();
        initChangeAssetButtons();
        initMusicButton();
        initNotificationManager();
        sendNotification();

    }

    private void initNotificationManager() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel("hu.janny.tomsschedule.timerstarted",
                CustomActivityHelper.isFixActivity(customActivityName)
                        ? getString(CustomActivityHelper.getStringResourceOfFixActivity(customActivityName))
                        : customActivityName,
                "Activity is in progress!");
    }

    private void initMusic() {
        if (musicOn && TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
            startMusicService();
            binding.musicOn.setVisibility(View.VISIBLE);
        } else if (!musicOn && TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
            binding.musicOn.setImageResource(R.drawable.ic_music_off);
            binding.musicOn.setVisibility(View.VISIBLE);
        } else if (TimerAssets.getAsset(currentAsset).getMusicResId() == 0) {
            binding.musicOn.setVisibility(View.INVISIBLE);
        }
    }

    private void initMusicButton() {
        binding.musicOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicOn) {
                    musicOn = false;
                    stopService(musicIntent);
                    binding.musicOn.setImageResource(R.drawable.ic_music_off);
                } else {
                    //musicOn = true;
                    startMusicService();
                    //binding.musicOn.setImageResource(R.drawable.ic_music_on);
                }
            }
        });
    }

    private void startCounterService() {
        counterIntent = new Intent(TimerActivity.this, CounterService.class);
        registerReceiver(broadcastReceiver, new IntentFilter(CounterService.BROADCAST_ACTION));
        startService(counterIntent);
    }

    private void startMusicService() {
        musicIntent = new Intent(TimerActivity.this, MusicPlayerService.class);
        musicIntent.putExtra(MusicPlayerService.MUSIC_RESOURCE, TimerAssets.getAsset(currentAsset).getMusicResId());
        musicOn = true;
        startService(musicIntent);
        binding.musicOn.setImageResource(R.drawable.ic_music_on);
    }

    private void initTodayInMillis() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        cal.clear();
        cal.set(year, month, day);
        todayMillis = cal.getTimeInMillis();
    }

    private void initChangeAssetButtons() {
        binding.nextAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentAsset = (currentAsset + 1) % maxAssetNum;
                setUIAsset();
                changeAssetMusicHandling();
            }
        });

        binding.prevAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentAsset = currentAsset == 0 ? maxAssetNum - 1 : (currentAsset - 1) % (maxAssetNum - 1);
                setUIAsset();
                changeAssetMusicHandling();
            }
        });
    }

    private void changeAssetMusicHandling() {
        if (musicOn) {
            stopService(musicIntent);
        }
        if (TimerAssets.getAsset(currentAsset).getMusicResId() != 0 && musicOn) {
            stopService(musicIntent);
            startMusicService();
        }
        if (TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
            binding.musicOn.setVisibility(View.VISIBLE);
        } else {
            binding.musicOn.setVisibility(View.INVISIBLE);
        }
    }

    private void setUIAsset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColor()));
        }
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColor())));
        binding.timerLayout.setBackground(AppCompatResources.getDrawable(this, TimerAssets.getAsset(currentAsset).getBgResId()));
        binding.themeName.setText(TimerAssets.getAsset(currentAsset).getNameResId());
        binding.timerButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColor())));
        binding.nextAsset.setColorFilter(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()), android.graphics.PorterDuff.Mode.MULTIPLY);
        binding.prevAsset.setColorFilter(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()), android.graphics.PorterDuff.Mode.MULTIPLY);
        binding.musicOn.setColorFilter(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()), android.graphics.PorterDuff.Mode.MULTIPLY);
        binding.todayText.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.today.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.soFar.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.soFarText.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.remaining.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.remainingText.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.themeName.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
    }

    protected void createNotificationChannel(String id, String name, String description) {

        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel =
                new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        channel.enableLights(true);
        channel.setLightColor(R.color.toms_400);
        channel.enableVibration(true);
        channel.setVibrationPattern(
                new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(channel);
    }

    private void sendNotification() {

        int notificationID = 101;

        Intent resultIntent = new Intent(this, TimerActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        final Icon icon = Icon.createWithResource(TimerActivity.this,
                android.R.drawable.ic_dialog_info);
        Notification.Action action =
                new Notification.Action.Builder(icon, "Finish", pendingIntent)
                        .build();

        String channelID = "hu.janny.tomsschedule.timerstarted";
        Notification notification =
                new Notification.Builder(TimerActivity.this,
                        channelID)
                        .setContentTitle(CustomActivityHelper.isFixActivity(customActivityName)
                                ? getString(CustomActivityHelper.getStringResourceOfFixActivity(customActivityName))
                                : customActivityName)
                        .setContentText("Activity is in progress!")
                        .setSmallIcon(R.drawable.ic_timer)
                        .setChannelId(channelID)
                        .setContentIntent(pendingIntent)
                        .setActions(action)
                        .build();
        notificationManager.notify(notificationID, notification);
    }

    private void initStopButton() {
        binding.timerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handler.removeCallbacks(sendUpdatesToUI);
                if (musicOn && TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
                    stopService(musicIntent);
                }
                if (started) {
                    /*unregisterReceiver(broadcastReceiver);
                    stopService(counterIntent);*/
                    started = false;
                    String channelID = "hu.janny.tomsschedule.timerstarted";
                    notificationManager.deleteNotificationChannel(channelID);

                    if (currentTime > (24L * 60L * 60L * 1000L)) {
                        currentTime = 24L * 60L * 60L * 1000L;
                    }
                    long fullMinutes = currentTime % 60000L;
                    if (fullMinutes != 0L) {
                        currentTime = (currentTime / 60000L) * 60000L + 60000L;
                    }
                    ActivityTime activityTime = new ActivityTime(customActivityId, todayMillis, currentTime);
                    if (CustomActivityHelper.isFixActivity(customActivityName)) {
                        int isInsert = mainViewModel.insertOrUpdateTime(activityTime);
                        while (isInsert == 0) {
                            isInsert = mainViewModel.insertOrUpdateTime(activityTime);
                        }
                        if (isInsert == 1) {
                            // add to Firebase
                            FirebaseManager.saveInsertedActivityTimeToFirebase(activityTime, customActivityName, currentUser);
                        } else if (isInsert == 2) {
                            // update in Firebase
                            FirebaseManager.saveUpdateActivityTimeToFirebase(activityTime, customActivityName, currentUser);
                        }
                    } else {
                        mainViewModel.insertOrUpdateTimeSingle(activityTime);
                    }
                    updateActivity(activityTime);
                    mainViewModel.updateActivity(customActivity);
                    System.out.println(currentTime);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.remove(TIMER_INITIAL_MILLIS);
                    editor.apply();
                    Intent intent = new Intent(TimerActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void updateActivity(ActivityTime activityTime) {
        long todayMillis = CustomActivityHelper.todayMillis();
        long firstDayOfThisMonth = CustomActivityHelper.firstDayOfThisMonth();
        long thisMonday = CustomActivityHelper.thisMondayMillis();
        customActivity.setaT(customActivity.getaT() + activityTime.getT());

        switch (customActivity.gettN()) {
            case 2:
                if (customActivity.ishFD()) {
                    if (activityTime.getD() == todayMillis && CustomActivityHelper.todayIsAFixedDayAndWhat(customActivity.getCustomWeekTime()) != 0
                            && customActivity.geteD() == 0L) {
                        if (customActivity.getlD() != todayMillis) {
                            customActivity.setsF(activityTime.getT());
                            setRemainingFieldFixedDays(activityTime.getT());
                        } else {
                            customActivity.setsF(customActivity.getsF() + activityTime.getT());
                            setRemainingFieldUpdate(activityTime.getT());
                        }
                    } else if (activityTime.getD() == todayMillis && CustomActivityHelper.todayIsAFixedDayAndWhat(customActivity.getCustomWeekTime()) != 0
                            && customActivity.getsD() == 0L && customActivity.geteD() != 0L && customActivity.getlD() < customActivity.geteD()) {
                        if (customActivity.getlD() != todayMillis) {
                            customActivity.setsF(activityTime.getT());
                            setRemainingFieldFixedDays(activityTime.getT());
                        } else {
                            customActivity.setsF(customActivity.getsF() + activityTime.getT());
                            setRemainingFieldUpdate(activityTime.getT());
                        }
                    }
                } else {
                    if (activityTime.getD() == todayMillis) {
                        if (customActivity.getlD() != todayMillis) {
                            customActivity.setsF(activityTime.getT());
                            setRemainingFieldInsert(activityTime.getT());
                        } else {
                            customActivity.setsF(customActivity.getsF() + activityTime.getT());
                            setRemainingFieldUpdate(activityTime.getT());
                        }
                    }
                }
            case 3:
                if (customActivity.geteD() == 0L) {
                    if (activityTime.getD() >= firstDayOfThisMonth && customActivity.getlD() < firstDayOfThisMonth) {
                        customActivity.setsF(activityTime.getT());
                        setRemainingFieldInsert(activityTime.getT());
                    } else if (activityTime.getD() > firstDayOfThisMonth && customActivity.getlD() >= firstDayOfThisMonth) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        setRemainingFieldUpdate(activityTime.getT());
                    }
                } else {
                    if (activityTime.getD() >= firstDayOfThisMonth && customActivity.getlD() < firstDayOfThisMonth && todayMillis <= customActivity.geteD()) {
                        customActivity.setsF(activityTime.getT());
                        setRemainingFieldInsert(activityTime.getT());
                    } else if (activityTime.getD() > firstDayOfThisMonth && customActivity.getlD() >= firstDayOfThisMonth && todayMillis < customActivity.geteD()) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        setRemainingFieldUpdate(activityTime.getT());
                    }
                }
            case 4:
                if (customActivity.geteD() == 0L) {
                    if (activityTime.getD() >= thisMonday && customActivity.getlD() < thisMonday) {
                        customActivity.setsF(activityTime.getT());
                        setRemainingFieldInsert(activityTime.getT());
                    } else if (activityTime.getD() > thisMonday && customActivity.getlD() >= thisMonday) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        setRemainingFieldUpdate(activityTime.getT());
                    }
                } else {
                    if (activityTime.getD() >= thisMonday && customActivity.getlD() < thisMonday && todayMillis <= customActivity.geteD()) {
                        customActivity.setsF(activityTime.getT());
                        setRemainingFieldInsert(activityTime.getT());
                    } else if (activityTime.getD() > thisMonday && customActivity.getlD() >= thisMonday && todayMillis < customActivity.geteD()) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        setRemainingFieldUpdate(activityTime.getT());
                    }
                }
            case 5:
                if (customActivity.geteD() == 0L) {
                    if (customActivity.getsF() < customActivity.getDur()) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        setRemainingFieldUpdate(activityTime.getT());
                    }
                } else {
                    if (customActivity.getsF() < customActivity.getDur() && todayMillis < customActivity.geteD()) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        setRemainingFieldUpdate(activityTime.getT());
                    }
                }
            case 6:
                if (todayMillis >= customActivity.getsD() && todayMillis <= customActivity.geteD()) {
                    customActivity.setsF(customActivity.getsF() + activityTime.getT());
                }
            case 7:
                System.out.println(todayMillis + " " + customActivity.getsD() + " " + customActivity.geteD() + " " + customActivity.getlD());
                if (todayMillis >= customActivity.getsD() && todayMillis <= customActivity.geteD() && customActivity.getlD() < customActivity.getsD()) {
                    customActivity.setsF(activityTime.getT());
                    setRemainingFieldInsert(activityTime.getT());
                } else if (todayMillis >= customActivity.getsD() && todayMillis <= customActivity.geteD() && customActivity.getlD() >= customActivity.getsD()) {
                    customActivity.setsF(customActivity.getsF() + activityTime.getT());
                    setRemainingFieldUpdate(activityTime.getT());
                }
            case 8:
                if (customActivity.geteD() == 0L) {
                    if (activityTime.getD() >= thisMonday && CustomActivityHelper.todayIsAFixedDayAndWhat(customActivity.getCustomWeekTime()) != 0 && activityTime.getD() == todayMillis) {
                        if (customActivity.getlD() != todayMillis) {
                            customActivity.setsF(activityTime.getT());
                            customActivity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(customActivity.getCustomWeekTime()) - activityTime.getT()), 0L));
                        } else {
                            customActivity.setsF(customActivity.getsF() + activityTime.getT());
                            customActivity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(customActivity.getCustomWeekTime()) - activityTime.getT()), 0L));
                        }
                    }
                }

        }

        if (customActivity.getlD() < activityTime.getD()) {
            customActivity.setlD(activityTime.getD());
        }
    }

    private void setRemainingFieldFixedDays(long time) {
        customActivity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(customActivity.getCustomWeekTime()) - time), 0L));
    }

    private void setRemainingFieldInsert(long time) {
        customActivity.setRe(Math.max((customActivity.getDur() - time), 0L));
    }

    private void setRemainingFieldUpdate(long time) {
        customActivity.setRe(Math.max((customActivity.getRe() - time), 0L));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    private void updateUI(Intent intent) {
        long time = intent.getLongExtra("time", 0);
        currentTime = time;
        //System.out.println( "Time " + time + " " + DateConverter.durationConverterFromLongToStringToTimer(time - 3L));
        binding.timerTextView.setText(DateConverter.durationConverterFromLongToStringToTimer(time));
        binding.today.setText(DateConverter.durationConverterFromLongToStringToTimer(time));
    }

    private void updateUIHere() {
        currentTime = SystemClock.uptimeMillis() - initialMillis;
        //System.out.println( "Time " + time + " " + DateConverter.durationConverterFromLongToStringToTimer(time - 3L));
        binding.timerTextView.setText(DateConverter.durationConverterFromLongToStringToTimer(currentTime));
        binding.today.setText(DateConverter.durationConverterFromLongToStringToTimer(currentTime));
    }

    @Override
    protected void onStart() {
        super.onStart();
        initMusic();
        handler.postDelayed(sendUpdatesToUI, 1000);
        initialMillis = sharedPref.getLong(TIMER_INITIAL_MILLIS, SystemClock.uptimeMillis());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(musicOn && TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
            stopService(musicIntent);
        }
        handler.removeCallbacks(sendUpdatesToUI);
        /*SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong();
        editor.apply();*/
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            updateUIHere();
            handler.postDelayed(this, 1000); // 1 seconds
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("ondestroy timeractivity");
        /*if(musicOn) {
            stopService(musicIntent);
        }
        if(started) {
            unregisterReceiver(broadcastReceiver);
            stopService(counterIntent);

            long fullMinutes = currentTime % 60000;
            if(fullMinutes != 0L) {
                currentTime = (currentTime / 60000) * 60000 + 60000;
            }
            ActivityTime activityTime = new ActivityTime(customActivityId, todayMillis, currentTime);
            mainViewModel.insertOrUpdateTimeSingle(activityTime);
            String channelID = "hu.janny.tomsschedule.timerstarted";
            notificationManager.deleteNotificationChannel(channelID);
        }*/

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(ASSET_NUM, currentAsset);
        editor.putBoolean(MUSIC_ON, musicOn);
        editor.apply();
    }
}