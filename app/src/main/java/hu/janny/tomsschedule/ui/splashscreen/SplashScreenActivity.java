package hu.janny.tomsschedule.ui.splashscreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;

import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.ui.loginregister.LoginActivity;

/**
 * This activity shows the splash screen and checks whether the user is already logged in or not.
 * If they are not logged in, it directs to the login activity, if they are, it directs to the home page.
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Shows the splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Prevents this activity to be visible
        splashScreen.setKeepOnScreenCondition(() -> true );

        // Initializes Firebase manager
        FirebaseManager.onStart();

        // Checks whether the user is logged in or not and directs to the appropriate activity
        if(FirebaseManager.isUserLoggedIn()){
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        }

        finish();
    }
}