package hu.janny.tomsschedule;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.google.android.material.navigation.NavigationView;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import hu.janny.tomsschedule.databinding.ActivityMainBinding;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.ui.main.addcustomactivity.AddCustomActivityFragment;

public class MainActivity extends AppCompatActivity implements AddCustomActivityFragment.OnFragmentInteractionListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private LoginRegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);

        setSupportActionBar(binding.appBarMain.toolbar);

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

        /*binding.fragmentHome.addCustomActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(
                        R.id.action_nav_home_to_add_custom_activity);
                *//*String key = FirebaseManager.database.getReference("customactivities")
                        .child(FirebaseManager.user.getUid()).push().getKey();
                CustomActivity activity = new CustomActivity("name", "#FF00FF", "note", 5, false, false, false);

                Map<String, Object> activityValues = activity.toMap();
                FirebaseManager.database.getReference().child("customactivities").child(FirebaseManager.auth.getUid()).child(key).setValue(activity)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this,"Added to db!",Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this,"Failed adding to db!",Toast.LENGTH_LONG).show();
                            }
                        });
*//*
            }
        });*/
    }

    public void addNewCustomActivity(View view) {
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(
                R.id.action_nav_home_to_add_custom_activity);
    }

    public void goBackToHome(View view) {
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(
                R.id.action_add_custom_activity_to_nav_home);
    }

    public void goBackToHomeAfterDelete(View view) {
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main).navigate(
                R.id.action_detailFragment_to_nav_home);
    }

    public void logoutUser(View view) {
        viewModel.logoutUser(FirebaseManager.user.getUid());
        FirebaseManager.logoutUser();

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
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