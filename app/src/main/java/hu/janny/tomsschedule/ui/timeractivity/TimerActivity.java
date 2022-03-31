package hu.janny.tomsschedule.ui.timeractivity;

import android.graphics.Color;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.ActivityTimerBinding;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.model.helper.TimerAssets;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.ui.splashscreen.SplashScreenActivity;
import hu.janny.tomsschedule.viewmodel.MainViewModel;

/**
 * This activity is for counting the time spent by user on the custom activity.
 * It includes a music intent, that allows to play some hardcoded music during the activity.
 * It stops, when the activity is paused.
 * It also includes a notification manager, which shows a notification while the activity is in progress.
 */
public class TimerActivity extends AppCompatActivity {

    public final static String ACTIVITY_ID = "activity_id";
    public final static String ACTIVITY_NAME = "activity_name";
    public final static String TODAY_SPENT = "today_spent";
    private final String TIMER_INITIAL_MILLIS = "timer_initial_millis";
    private final String ASSET_NUM = "asset_num";
    private final String MUSIC_ON = "music_on";

    private ActivityTimerBinding binding;
    private MainViewModel mainViewModel;

    private SharedPreferences sharedPref;

    private long customActivityId;
    private String customActivityName;

    private CustomActivity customActivity;
    private User currentUser;

    private int currentAsset;
    private boolean musicOn;
    private boolean musicPlaying;
    private int maxAssetNum;
    // The time spent with this activity from starting the timer
    private long currentTime = 0L;
    private boolean showOtherData = false;

    private ActionBar actionBar;
    private Intent musicIntent;
    private NotificationManager notificationManager;

    private long initialMillis;

    // It helps refreshing the UI in every 1 second
    private Handler handler = new Handler();

    // The activity has started
    public static boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binds layout
        binding = ActivityTimerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Gets a MainViewModel instance
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        actionBar = getSupportActionBar();
        maxAssetNum = TimerAssets.maxAssetNum();

        sharedPref = getApplicationContext().getSharedPreferences(
                "hu.janny.tomsschedule.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        // Index of the last shown timer asset
        currentAsset = sharedPref.getInt(ASSET_NUM, 0);
        // Music was turned on or off last time
        musicOn = sharedPref.getBoolean(MUSIC_ON, true);
        // If the activity is already in progress that will not be 0L
        initialMillis = sharedPref.getLong(TIMER_INITIAL_MILLIS, 0L);
        if (initialMillis == 0L) {
            initialMillis = SystemClock.uptimeMillis();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(TIMER_INITIAL_MILLIS, initialMillis);
            editor.apply();
        }

        setUIAsset();

        // Gets parameters from bundle
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            customActivityId = extras.getLong(ACTIVITY_ID);
            customActivityName = extras.getString(ACTIVITY_NAME);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(ACTIVITY_ID, customActivityId);
            editor.putString(ACTIVITY_NAME, customActivityName);
            editor.apply();
        } else {
            customActivityId = sharedPref.getLong(ACTIVITY_ID, 0L);
            customActivityName = sharedPref.getString(ACTIVITY_NAME, "");
        }

        mainViewModel.findActivityById(customActivityId);

        // Observer of the activity of which we want to count the time
        mainViewModel.getSingleActivity().observe(this, new Observer<CustomActivity>() {
            @Override
            public void onChanged(CustomActivity activity) {
                if (activity != null) {
                    customActivity = activity;
                    // According to the type of activity we show or hide sofar and remaining texts and times
                    if (customActivity.gettN() == 1 || customActivity.gettN() == 6 ||
                            customActivity.gettN() == 5 || (customActivity.geteD() != 0L && customActivity.geteD() < CustomActivityHelper.todayMillis())) {
                        binding.soFar.setVisibility(View.GONE);
                        binding.remaining.setVisibility(View.GONE);
                        binding.soFarText.setVisibility(View.GONE);
                        binding.remainingText.setVisibility(View.GONE);
                        showOtherData = false;
                    } else {
                        binding.soFar.setVisibility(View.VISIBLE);
                        binding.remaining.setVisibility(View.VISIBLE);
                        binding.soFarText.setVisibility(View.VISIBLE);
                        binding.remainingText.setVisibility(View.VISIBLE);
                        showOtherData = true;
                    }
                } else {
                    Toast.makeText(TimerActivity.this, getString(R.string.timer_no_activity), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(TimerActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Observer of the current (logged in) user
        mainViewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
            }
        });

        // The activity is starting now
        started = true;

        initStopButton();
        initChangeAssetButtons();
        initMusicButton();
        initNotificationManager();
        sendNotification();

    }

    /**
     * Initializes music playing service according to the music of the asset and the music is turned on or not.
     */
    private void initMusic() {
        if (musicOn && TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
            // Asset has music and music is turned on
            startMusicService();
            binding.musicOn.setImageResource(R.drawable.ic_music_on);
            binding.musicOn.setVisibility(View.VISIBLE);
            musicPlaying = true;
        } else if (!musicOn && TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
            // Asset has music and music is turned off
            binding.musicOn.setImageResource(R.drawable.ic_music_off);
            binding.musicOn.setVisibility(View.VISIBLE);
            musicPlaying = false;
        } else if (TimerAssets.getAsset(currentAsset).getMusicResId() == 0) {
            // Asset has not music
            binding.musicOn.setVisibility(View.INVISIBLE);
            musicPlaying = false;
        }
    }

    /**
     * Initializes the button with which we are able to turn music on and off.
     */
    private void initMusicButton() {
        binding.musicOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicOn) {
                    // Turn off
                    musicOn = false;
                    if (musicPlaying) {
                        stopService(musicIntent);
                        musicPlaying = false;
                    }
                    binding.musicOn.setImageResource(R.drawable.ic_music_off);
                } else {
                    // Turn on
                    musicOn = true;
                    if (!musicPlaying) {
                        startMusicService();
                        musicPlaying = true;
                    }
                    binding.musicOn.setImageResource(R.drawable.ic_music_on);
                }
            }
        });
    }

    /**
     * Starts the music playing service.
     */
    private void startMusicService() {
        musicIntent = new Intent(TimerActivity.this, MusicPlayerService.class);
        musicIntent.putExtra(MusicPlayerService.MUSIC_RESOURCE, TimerAssets.getAsset(currentAsset).getMusicResId());
        startService(musicIntent);
    }

    /**
     * Initializes the change asset buttons.
     */
    private void initChangeAssetButtons() {
        // Next asset button
        binding.nextAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentAsset = (currentAsset + 1) % maxAssetNum;
                setUIAsset();
                changeAssetMusicHandling();
            }
        });
        // Previous asset button
        binding.prevAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentAsset = currentAsset == 0 ? maxAssetNum - 1 : (currentAsset - 1) % (maxAssetNum - 1);
                setUIAsset();
                changeAssetMusicHandling();
            }
        });
    }

    /**
     * Updates the music played based on the new asset.
     */
    private void changeAssetMusicHandling() {
        // Stops the current music
        if (musicPlaying) {
            stopService(musicIntent);
            musicPlaying = false;
        }
        // Starts the new music if there is music in the asset and the music is on
        if (TimerAssets.getAsset(currentAsset).getMusicResId() != 0 && musicOn) {
            startMusicService();
            binding.musicOn.setImageResource(R.drawable.ic_music_on);
            musicPlaying = true;
        } else {
            binding.musicOn.setImageResource(R.drawable.ic_music_off);
        }
        // Updates the UI based on the availability of music in the asset
        if (TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
            binding.musicOn.setVisibility(View.VISIBLE);
        } else {
            binding.musicOn.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Sets up the UI based on the chosen timer asset. It sets background and the colour of texts, buttons and action bar.
     */
    private void setUIAsset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(darkenColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColor())));
        }
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColor())));
        binding.timerLayout.setBackground(AppCompatResources.getDrawable(this, TimerAssets.getAsset(currentAsset).getBgResId()));
        binding.timerButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColor())));
        // Theme
        binding.themeName.setText(TimerAssets.getAsset(currentAsset).getNameResId());
        binding.themeName.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.nextAsset.setColorFilter(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()), android.graphics.PorterDuff.Mode.MULTIPLY);
        binding.prevAsset.setColorFilter(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()), android.graphics.PorterDuff.Mode.MULTIPLY);
        binding.musicOn.setColorFilter(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()), android.graphics.PorterDuff.Mode.MULTIPLY);
        // Texts
        //binding.todayText.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        //binding.today.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.soFar.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.soFarText.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.remaining.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
        binding.remainingText.setTextColor(ContextCompat.getColor(this, TimerAssets.getAsset(currentAsset).getColorOfText()));
    }

    /**
     * Darkens the given colour.
     *
     * @param color the colour to be darker
     * @return color int of the darker colour
     */
    @ColorInt
    int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    /**
     * Initializes notification manager to show notification when the time count is in progress.
     */
    private void initNotificationManager() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel("hu.janny.tomsschedule.timerstarted",
                CustomActivityHelper.isFixActivity(customActivityName)
                        ? getString(CustomActivityHelper.getStringResourceOfFixActivity(customActivityName))
                        : customActivityName,
                getString(R.string.timer_activity_is_in_progress));
    }

    /**
     * Creates the notification channel for the timer activity.
     *
     * @param id          the id of the channel
     * @param name        the name of the custom activity
     * @param description the description to be shown in notification
     */
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

    /**
     * Sends the notification of activity is in progress.
     */
    private void sendNotification() {

        int notificationID = 101;

        Intent resultIntent = new Intent(this, SplashScreenActivity.class);
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
                new Notification.Action.Builder(icon, getString(R.string.timer_finish), pendingIntent)
                        .build();

        String channelID = "hu.janny.tomsschedule.timerstarted";
        Notification notification =
                new Notification.Builder(TimerActivity.this,
                        channelID)
                        .setContentTitle(CustomActivityHelper.isFixActivity(customActivityName)
                                ? getString(CustomActivityHelper.getStringResourceOfFixActivity(customActivityName))
                                : customActivityName)
                        .setContentText(getString(R.string.timer_activity_is_in_progress))
                        .setSmallIcon(R.drawable.ic_timer)
                        .setChannelId(channelID)
                        .setContentIntent(pendingIntent)
                        .setActions(action)
                        .setOngoing(true)
                        .build();
        notificationManager.notify(notificationID, notification);
    }

    /**
     * Initializes stop counting time button. It saves the time into database.
     */
    private void initStopButton() {
        binding.timerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Stops updating the UI
                handler.removeCallbacks(sendUpdatesToUI);
                // Stops music
                if (musicOn && TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
                    stopService(musicIntent);
                    musicPlaying = false;
                }
                if (started) {
                    started = false;
                    // Deletes notification
                    String channelID = "hu.janny.tomsschedule.timerstarted";
                    notificationManager.deleteNotificationChannel(channelID);

                    if (currentTime > (24L * 60L * 60L * 1000L)) {
                        currentTime = 24L * 60L * 60L * 1000L;
                    }
                    // We store just minutes. It rounded up.
                    long fullMinutes = currentTime % 60000L;
                    if (fullMinutes != 0L) {
                        currentTime = (currentTime / 60000L) * 60000L + 60000L;
                    }

                    ActivityTime activityTime = new ActivityTime(customActivityId, CustomActivityHelper.todayMillis(), currentTime);
                    // Saves activity time into database(s)
                    mainViewModel.saveIntoDatabase(activityTime, customActivity, currentUser);
                    // Removes parameters from shared preference
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.remove(TIMER_INITIAL_MILLIS);
                    editor.remove(ACTIVITY_ID);
                    editor.remove(ACTIVITY_NAME);
                    editor.apply();

                    Intent intent = new Intent(TimerActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    /**
     * Updates the UI in every 1 second.
     */
    private final Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            updateUI();
            handler.postDelayed(this, 1000); // 1 seconds
        }
    };

    /**
     * Updates the UI according to the time. It updates clocks and time counters.
     */
    private void updateUI() {
        currentTime = SystemClock.uptimeMillis() - initialMillis;
        binding.timerTextView.setText(DateConverter.durationConverterFromLongToStringToTimer(currentTime));
        //binding.today.setText(DateConverter.durationConverterFromLongToStringToTimer(today + currentTime));
        if(showOtherData) {
            binding.soFar.setText(DateConverter.durationConverterFromLongToStringToTimer(customActivity.getsF() + currentTime));
            binding.remaining.setText(DateConverter.durationConverterFromLongToStringToTimer(customActivity.getRe() - currentTime));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Starts music
        initMusic();
        // Starts updating UI
        handler.postDelayed(sendUpdatesToUI, 1000);
        initialMillis = sharedPref.getLong(TIMER_INITIAL_MILLIS, SystemClock.uptimeMillis());
        customActivityId = sharedPref.getLong(ACTIVITY_ID, 0L);
        customActivityName = sharedPref.getString(ACTIVITY_NAME, "");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stops music
        if (musicPlaying && TimerAssets.getAsset(currentAsset).getMusicResId() != 0) {
            stopService(musicIntent);
            musicPlaying = false;
        }
        // Stops updating the UI
        handler.removeCallbacks(sendUpdatesToUI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(ASSET_NUM, currentAsset);
        editor.putBoolean(MUSIC_ON, musicOn);
        editor.apply();

        binding = null;
    }
}