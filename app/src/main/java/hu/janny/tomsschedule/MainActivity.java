package hu.janny.tomsschedule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import hu.janny.tomsschedule.databinding.ActivityMainBinding;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.ui.loginregister.LoginActivity;
import hu.janny.tomsschedule.ui.main.addcustomactivity.AddCustomActivityFragment;
import hu.janny.tomsschedule.ui.timeractivity.TimerActivity;
import hu.janny.tomsschedule.viewmodel.LoginRegisterViewModel;

public class MainActivity extends AppCompatActivity implements AddCustomActivityFragment.OnFragmentInteractionListener {

    private ActivityMainBinding binding;
    private LoginRegisterViewModel viewModel;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If an activity is in progress we have to go ack to timer activity
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                "hu.janny.tomsschedule.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE);
        long activityId = sharedPref.getLong(TimerActivity.ACTIVITY_ID, 0L);
        String activityName = sharedPref.getString(TimerActivity.ACTIVITY_NAME, "");
        if (activityId != 0L && !activityName.equals("")) {
            startTimerActivity(activityId, activityName);
        }

        // Binds layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Gets a LoginRegisterViewModel
        viewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);

        setSupportActionBar(binding.appBarMain.toolbar);
        // Navigation drawer
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_statistics, R.id.nav_settings, R.id.nav_account)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    /**
     * Navigates to add new custom activity when the add new activity fab is pressed.
     *
     * @param view the view of the activity
     */
    public void addNewCustomActivity(View view) {
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(
                R.id.action_nav_home_to_add_custom_activity);
    }

    /**
     * Logs out the current user from local and Firebase database.
     *
     * @param view the view of the activity
     */
    public void logoutUser(View view) {
        viewModel.logoutUser(FirebaseManager.user.getUid());
        FirebaseManager.logoutUser();

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    /**
     * Starts the timer activity.
     *
     * @param customActivityId the custom activity we want to count
     * @param activityName     the name of the custom activity we want to count
     */
    public void startTimerActivity(long customActivityId, String activityName) {
        Intent i = new Intent(this, TimerActivity.class);
        i.putExtra(TimerActivity.ACTIVITY_ID, customActivityId);
        i.putExtra(TimerActivity.ACTIVITY_NAME, activityName);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}