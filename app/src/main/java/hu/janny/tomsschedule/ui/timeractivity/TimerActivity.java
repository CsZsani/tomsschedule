package hu.janny.tomsschedule.ui.timeractivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.ActivityTimerBinding;
import hu.janny.tomsschedule.model.ActivityTime;
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
    private long currentTime = 0L;
    private MainViewModel mainViewModel;
    private User currentUser;
    private int currentAsset;
    private int maxAssetNum;
    ActionBar actionBar;

    //LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

    Intent intent;
    NotificationManager notificationManager;
    long todayMillis;

    boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_timer);
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding = ActivityTimerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        actionBar = getSupportActionBar();

        maxAssetNum = TimerAssets.maxAssetNum();
        sharedPref = getApplicationContext().getSharedPreferences(
                "hu.janny.tomsschedule.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        currentAsset = sharedPref.getInt("asset_num", 0);
        setUIAsset();

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        customActivityId = extras.getLong(ACTIVITY_ID);
        customActivityName = extras.getString(ACTIVITY_NAME);

        intent = new Intent(TimerActivity.this, CounterService.class);
        registerReceiver(broadcastReceiver, new IntentFilter(CounterService.BROADCAST_ACTION));
        startService(intent);

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

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel("hu.janny.tomsschedule.timerstarted",
                CustomActivityHelper.isFixActivity(customActivityName)
                        ? getString(CustomActivityHelper.getStringResourceOfFixActivity(customActivityName))
                        : customActivityName,
                "Activity is in progress!");

        sendNotification();

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
            }
        });

        binding.prevAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentAsset = currentAsset == 0 ? maxAssetNum - 1 : (currentAsset - 1) % (maxAssetNum - 1);
                setUIAsset();
            }
        });
    }

    private void setUIAsset() {
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColor())));
        binding.timerLayout.setBackground(AppCompatResources.getDrawable(this, TimerAssets.getAsset(currentAsset).getBgResId()));
        binding.themeName.setText(TimerAssets.getAsset(currentAsset).getNameResId());
        binding.timerButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColor())));
        binding.nextAsset.setColorFilter(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()), android.graphics.PorterDuff.Mode.MULTIPLY);
        binding.prevAsset.setColorFilter(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()), android.graphics.PorterDuff.Mode.MULTIPLY);
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
                if(started) {
                    unregisterReceiver(broadcastReceiver);
                    stopService(intent);
                    started = false;
                    String channelID = "hu.janny.tomsschedule.timerstarted";
                    notificationManager.deleteNotificationChannel(channelID);

                    long fullMinutes = currentTime % 60000;
                    if(fullMinutes != 0L) {
                        currentTime = (currentTime / 60000) * 60000 + 60000;
                    }
                    ActivityTime activityTime = new ActivityTime(customActivityId, todayMillis, currentTime);
                    if(CustomActivityHelper.isFixActivity(customActivityName)) {
                        int isInsert = mainViewModel.insertOrUpdateTime(activityTime);
                        while(isInsert == 0) {
                            isInsert = mainViewModel.insertOrUpdateTime(activityTime);
                        }
                        if(isInsert == 1) {
                            // add to Firebase
                            FirebaseManager.saveInsertedActivityTimeToFirebase(activityTime, customActivityName, currentUser);
                        } else if(isInsert == 2){
                            // update in Firebase
                            FirebaseManager.saveUpdateActivityTimeToFirebase(activityTime, customActivityName, currentUser);
                        }
                    } else {
                        mainViewModel.insertOrUpdateTimeSingle(activityTime);
                    }
                    System.out.println(currentTime);
                    Intent intent = new Intent(TimerActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(started) {
            unregisterReceiver(broadcastReceiver);
            stopService(intent);

            long fullMinutes = currentTime % 60000;
            if(fullMinutes != 0L) {
                currentTime = (currentTime / 60000) * 60000 + 60000;
            }
            ActivityTime activityTime = new ActivityTime(customActivityId, todayMillis, currentTime);
            mainViewModel.insertOrUpdateTimeSingle(activityTime);
            String channelID = "hu.janny.tomsschedule.timerstarted";
            notificationManager.deleteNotificationChannel(channelID);
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("asset_num", currentAsset);
        editor.apply();
    }
}